import { applySortAndLimit, assertSingleFilterField } from './store.ts';
import type { Doc, Query, Store, Tx } from './store.ts';

/**
 * In-memory Store used by the tests. Mirrors Firestore semantics that matter to the API:
 *  - transactions are atomic and serialised (one at a time),
 *  - writes inside a transaction are buffered and discarded if the callback throws,
 *  - values are copied on the way in and out, so callers cannot mutate stored data by accident.
 */
export class MemoryStore implements Store {
  private readonly data = new Map<string, Doc>();
  private queue: Promise<unknown> = Promise.resolve();

  async get(path: string): Promise<Doc | null> {
    const d = this.data.get(path);
    return d ? structuredClone(d) : null;
  }

  async list(collection: string, query?: Query): Promise<Doc[]> {
    assertSingleFilterField(query);
    const prefix = `${collection}/`;
    const docs: Doc[] = [];
    for (const [path, doc] of this.data) {
      if (!path.startsWith(prefix) || path.slice(prefix.length).includes('/')) continue;
      const ok = (query?.where ?? []).every((w) => {
        const v = doc[w.field] as string | number | boolean | undefined;
        if (v === undefined) return false;
        if (w.op === '==') return v === w.value;
        if (w.op === '>=') return v >= w.value;
        return v <= w.value;
      });
      if (ok) docs.push(structuredClone(doc));
    }
    return applySortAndLimit(docs, query);
  }

  async set(path: string, doc: Doc): Promise<void> {
    this.data.set(path, structuredClone(doc));
  }

  async delete(path: string): Promise<void> {
    this.data.delete(path);
  }

  transaction<T>(fn: (tx: Tx) => Promise<T>): Promise<T> {
    const run = async (): Promise<T> => {
      const writes: Array<{ path: string; doc: Doc | null }> = [];
      const tx: Tx = {
        get: (path) => this.get(path),
        list: (collection, query) => this.list(collection, query),
        set: (path, doc) => void writes.push({ path, doc: structuredClone(doc) }),
        delete: (path) => void writes.push({ path, doc: null }),
      };
      const result = await fn(tx);
      for (const w of writes) {
        if (w.doc === null) this.data.delete(w.path);
        else this.data.set(w.path, w.doc);
      }
      return result;
    };
    // Serialise: each transaction starts after the previous one has finished (success or failure).
    const next = this.queue.then(run, run);
    this.queue = next.catch(() => undefined);
    return next;
  }
}
