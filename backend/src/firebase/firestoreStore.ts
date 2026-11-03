import { getFirestore } from 'firebase-admin/firestore';
import type { Firestore, Transaction, Query as FsQuery } from 'firebase-admin/firestore';
import type { App } from 'firebase-admin/app';
import { applySortAndLimit, assertSingleFilterField } from '../store/store.ts';
import type { Doc, Query, Store, Tx } from '../store/store.ts';

/**
 * Store backed by Cloud Firestore (a NoSQL document database), accessed only from the server.
 * Security rules deny all direct client access, so the API is the single door to user data.
 * Reference: https://firebase.google.com/docs/firestore/manage-data/transactions
 */
/** Firestore rejects `undefined` field values; a JSON round-trip drops them (all stored data is plain JSON). */
const clean = (doc: Doc): Doc => JSON.parse(JSON.stringify(doc)) as Doc;

export class FirestoreStore implements Store {
  private readonly db: Firestore;

  constructor(app: App) {
    this.db = getFirestore(app);
  }

  private build(collection: string, query?: Query): FsQuery {
    assertSingleFilterField(query);
    let q: FsQuery = this.db.collection(collection);
    for (const w of query?.where ?? []) q = q.where(w.field, w.op, w.value);
    return q;
  }

  async get(path: string): Promise<Doc | null> {
    const snap = await this.db.doc(path).get();
    return snap.exists ? (snap.data() as Doc) : null;
  }

  async list(collection: string, query?: Query): Promise<Doc[]> {
    const snap = await this.build(collection, query).get();
    return applySortAndLimit(snap.docs.map((d: { data(): unknown }) => d.data() as Doc), query);
  }

  async set(path: string, doc: Doc): Promise<void> {
    await this.db.doc(path).set(clean(doc));
  }

  async delete(path: string): Promise<void> {
    await this.db.doc(path).delete();
  }

  transaction<T>(fn: (tx: Tx) => Promise<T>): Promise<T> {
    return this.db.runTransaction(async (t: Transaction) => {
      const tx: Tx = {
        get: async (path) => {
          const snap = await t.get(this.db.doc(path));
          return snap.exists ? (snap.data() as Doc) : null;
        },
        list: async (collection, query) => {
          const snap = await t.get(this.build(collection, query));
          return applySortAndLimit(snap.docs.map((d: { data(): unknown }) => d.data() as Doc), query);
        },
        set: (path, doc) => void t.set(this.db.doc(path), clean(doc)),
        delete: (path) => void t.delete(this.db.doc(path)),
      };
      return fn(tx);
    });
  }
}
