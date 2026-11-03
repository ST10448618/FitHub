/**
 * Storage abstraction. The API talks only to this interface, so:
 *  - production uses Firestore (src/firebase/firestoreStore.ts), and
 *  - tests use an in-memory copy (memoryStore.ts) that behaves the same way.
 *
 * Paths are Firestore-style: a document is "users/{uid}", a collection is "users/{uid}/food_logs".
 * Every document carries its own "id" field.
 *
 * Index-free queries: all filters in one query must be on the SAME field, and sorting/limiting
 * happens after the filter. This means no Firestore composite indexes have to be created.
 */
export type Doc = Record<string, unknown>;

export type FilterOp = '==' | '>=' | '<=';

export interface Query {
  where?: Array<{ field: string; op: FilterOp; value: string | number | boolean }>;
  sortBy?: { field: string; direction?: 'asc' | 'desc' };
  limit?: number;
}

/** Operations available inside a transaction. Reads first, then writes (a Firestore rule). */
export interface Tx {
  get(path: string): Promise<Doc | null>;
  list(collection: string, query?: Query): Promise<Doc[]>;
  /** Buffered: applied only if the whole transaction succeeds. */
  set(path: string, doc: Doc): void;
  delete(path: string): void;
}

export interface Store extends Pick<Tx, 'get' | 'list'> {
  set(path: string, doc: Doc): Promise<void>;
  delete(path: string): Promise<void>;
  /**
   * Runs fn atomically. fn may be run more than once (Firestore retries on contention),
   * so it must not have side effects outside the Tx.
   */
  transaction<T>(fn: (tx: Tx) => Promise<T>): Promise<T>;
}

/** Shared by both stores: apply filter checks, sort and limit to already-fetched documents. */
export function applySortAndLimit(docs: Doc[], query: Query | undefined): Doc[] {
  let out = docs;
  const sort = query?.sortBy;
  if (sort) {
    const dir = sort.direction === 'desc' ? -1 : 1;
    out = [...out].sort((a, b) => {
      const av = a[sort.field] as string | number | undefined;
      const bv = b[sort.field] as string | number | undefined;
      if (av === bv) return 0;
      if (av === undefined) return 1;
      if (bv === undefined) return -1;
      return av < bv ? -dir : dir;
    });
  }
  if (query?.limit !== undefined) out = out.slice(0, query.limit);
  return out;
}

export function assertSingleFilterField(query: Query | undefined): void {
  const fields = new Set((query?.where ?? []).map((w) => w.field));
  if (fields.size > 1) {
    throw new Error('Queries may filter on one field only (keeps Firestore index-free)');
  }
}
