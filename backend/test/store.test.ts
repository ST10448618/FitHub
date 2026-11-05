// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { MemoryStore } from '../src/store/memoryStore.ts';
import { resolveLww } from '../src/store/lww.ts';

test('MemoryStore stores and returns a document', async () => {
  const s = new MemoryStore();
  await s.set('users/alice', { id: 'alice', firstName: 'Alex' });
  assert.deepEqual(await s.get('users/alice'), { id: 'alice', firstName: 'Alex' });
});

test('MemoryStore list filters on a single field with range ops', async () => {
  const s = new MemoryStore();
  await s.set('users/alice/food_logs/a', { id: 'a', logDate: '2026-10-28', calories: 500 });
  await s.set('users/alice/food_logs/b', { id: 'b', logDate: '2026-10-29', calories: 600 });
  await s.set('users/alice/food_logs/c', { id: 'c', logDate: '2026-10-30', calories: 700 });
  const out = await s.list('users/alice/food_logs', {
    where: [
      { field: 'logDate', op: '>=', value: '2026-10-29' },
      { field: 'logDate', op: '<=', value: '2026-10-30' },
    ],
    sortBy: { field: 'logDate', direction: 'asc' },
  });
  assert.equal(out.length, 2);
  assert.equal(out[0]!.id, 'b');
  assert.equal(out[1]!.id, 'c');
});

test('MemoryStore transaction applies writes on success', async () => {
  const s = new MemoryStore();
  await s.transaction(async (tx) => {
    tx.set('users/alice', { id: 'alice', firstName: 'Alex' });
  });
  assert.equal((await s.get('users/alice'))?.firstName, 'Alex');
});

test('LWW: newer write applies', () => {
  const out = resolveLww(
    { updatedAt: '2026-10-30T10:00:00Z' },
    '2026-10-30T11:00:00Z',
    new Date('2026-10-30T12:00:00Z'),
  );
  assert.equal(out.apply, true);
  // updatedAt is normalised via Date#toISOString(), so compare with millisecond precision.
  assert.equal(new Date(out.updatedAt).toISOString(), '2026-10-30T11:00:00.000Z');
});

test('LWW: older write is rejected', () => {
  const out = resolveLww(
    { updatedAt: '2026-10-30T11:00:00Z' },
    '2026-10-30T10:00:00Z',
    new Date('2026-10-30T12:00:00Z'),
  );
  assert.equal(out.apply, false);
});

test('LWW: equal timestamp applies (current >=, idempotent byte-wise)', () => {
  // resolveLww uses `>=`: an equal-timestamp write re-writes the same document.
  // From the client's POV the response is identical, so this is safe in production.
  // (Design note: if a side effect is ever added inside the transaction, this
  //  behaviour must be tightened to strict `>`. See commit message.)
  const out = resolveLww(
    { updatedAt: '2026-10-30T10:00:00Z' },
    '2026-10-30T10:00:00Z',
    new Date('2026-10-30T12:00:00Z'),
  );
  assert.equal(out.apply, true);
});

test('LWW: missing existing document always applies', () => {
  const out = resolveLww(null, '2026-10-30T10:00:00Z', new Date('2026-10-30T12:00:00Z'));
  assert.equal(out.apply, true);
});