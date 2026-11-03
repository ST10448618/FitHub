import { test } from 'node:test';
import assert from 'node:assert/strict';
import { MemoryStore } from '../src/store/memoryStore.ts';
import { resolveLww } from '../src/store/lww.ts';

test('get/set/delete round trip and defensive copies', async () => {
  const s = new MemoryStore();
  const d = { id: 'a', n: 1 };
  await s.set('users/u1/items/a', d);
  d.n = 99; // mutating the original must not change the stored copy
  assert.deepEqual(await s.get('users/u1/items/a'), { id: 'a', n: 1 });
  await s.delete('users/u1/items/a');
  assert.equal(await s.get('users/u1/items/a'), null);
});

test('list returns only direct children, filtered, sorted and limited', async () => {
  const s = new MemoryStore();
  for (const [id, date] of [['a', '2026-10-01'], ['b', '2026-10-03'], ['c', '2026-10-02']] as const) {
    await s.set(`users/u1/logs/${id}`, { id, date });
  }
  await s.set('users/u1/logs/a/sub/x', { id: 'x', date: '2026-10-01' }); // deeper level, must be ignored
  await s.set('users/u2/logs/z', { id: 'z', date: '2026-10-01' }); // other user
  const all = await s.list('users/u1/logs', { sortBy: { field: 'date', direction: 'desc' } });
  assert.deepEqual(all.map((d) => d.id), ['b', 'c', 'a']);
  const range = await s.list('users/u1/logs', { where: [{ field: 'date', op: '>=', value: '2026-10-02' }, { field: 'date', op: '<=', value: '2026-10-02' }] });
  assert.deepEqual(range.map((d) => d.id), ['c']);
  const limited = await s.list('users/u1/logs', { sortBy: { field: 'date' }, limit: 2 });
  assert.deepEqual(limited.map((d) => d.id), ['a', 'c']);
});

test('queries on two different fields are refused (keeps Firestore index-free)', async () => {
  const s = new MemoryStore();
  await assert.rejects(s.list('c', { where: [{ field: 'a', op: '==', value: 1 }, { field: 'b', op: '==', value: 2 }] }));
});

test('a failing transaction writes nothing', async () => {
  const s = new MemoryStore();
  await assert.rejects(
    s.transaction(async (tx) => {
      tx.set('c/a', { id: 'a' });
      throw new Error('boom');
    }),
  );
  assert.equal(await s.get('c/a'), null);
});

test('concurrent transactions do not lose updates', async () => {
  const s = new MemoryStore();
  await s.set('c/counter', { id: 'counter', n: 0 });
  const bump = () =>
    s.transaction(async (tx) => {
      const cur = (await tx.get('c/counter')) as { n: number };
      await new Promise((r) => setTimeout(r, 2)); // widen the race window
      tx.set('c/counter', { id: 'counter', n: cur.n + 1 });
    });
  await Promise.all(Array.from({ length: 20 }, bump));
  assert.equal(((await s.get('c/counter')) as { n: number }).n, 20);
});

test('last-write-wins: older write is rejected, equal and newer are applied', () => {
  const now = new Date('2026-10-30T10:00:00Z');
  const stored = { updatedAt: '2026-10-30T09:00:00.000Z' };
  assert.equal(resolveLww(stored, '2026-10-30T08:59:59Z', now).apply, false);
  assert.equal(resolveLww(stored, '2026-10-30T09:00:00Z', now).apply, true);
  assert.equal(resolveLww(stored, '2026-10-30T09:30:00Z', now).apply, true);
  assert.equal(resolveLww(null, '2020-01-01T00:00:00Z', now).apply, true);
});

test('timestamps far in the future are clamped to server time + 5 minutes', () => {
  const now = new Date('2026-10-30T10:00:00Z');
  assert.equal(resolveLww(null, '2030-01-01T00:00:00Z', now).updatedAt, '2026-10-30T10:05:00.000Z');
});
