import { test } from 'node:test';
import assert from 'node:assert/strict';
import { ApiError } from '../src/http/errors.ts';
import { Validator, isSafeId, isValidDate } from '../src/http/validate.ts';

test('collects every problem instead of stopping at the first', () => {
  const v = new Validator({ name: '   ', age: 0, height: 'tall' });
  v.str('name', { min: 1 });
  v.num('age', { min: 1, int: true });
  v.num('height');
  assert.equal(v.errors.length, 3);
  assert.throws(() => v.finish(), (e: unknown) => e instanceof ApiError && e.status === 400 && e.details?.length === 3);
});

test('trims strings and rejects blank required text', () => {
  const v = new Validator({ a: '  hi  ', b: '\t' });
  assert.equal(v.str('a', { min: 1 }), 'hi');
  v.str('b', { min: 1 });
  assert.equal(v.errors[0]?.field, 'b');
});

test('numbers must be finite numbers, not numeric strings', () => {
  const v = new Validator({ a: '5', b: Number.NaN, c: 2.5 });
  v.num('a');
  v.num('b');
  v.num('c', { int: true });
  assert.equal(v.errors.length, 3);
});

test('optNum: null stays null (unavailable) and undefined stays undefined', () => {
  const v = new Validator({ a: null, c: 0 });
  assert.equal(v.optNum('a'), null);
  assert.equal(v.optNum('b'), undefined);
  assert.equal(v.optNum('c', { min: 0 }), 0); // zero is a real value, distinct from null
  assert.equal(v.errors.length, 0);
});

test('exclusiveMin rejects the boundary', () => {
  const v = new Validator({ a: 0 });
  v.num('a', { min: 0, exclusiveMin: true });
  assert.equal(v.errors.length, 1);
});

test('dates must be real calendar dates', () => {
  assert.equal(isValidDate('2026-02-28'), true);
  assert.equal(isValidDate('2026-02-30'), false);
  assert.equal(isValidDate('2026-13-01'), false);
  assert.equal(isValidDate('30-10-2026'), false);
});

test('timestamps need an offset and are normalised to UTC', () => {
  const ok = new Validator({ t: '2026-10-30T12:00:00+02:00' });
  assert.equal(ok.timestamp('t'), '2026-10-30T10:00:00.000Z');
  const bad = new Validator({ t: '2026-10-30T12:00:00' });
  bad.timestamp('t');
  assert.equal(bad.errors.length, 1);
});

test('enum, boolean and email checks', () => {
  const v = new Validator({ g: 'ROBOT', b: 'yes', e: 'nope' });
  v.oneOf('g', ['MALE', 'FEMALE']);
  v.bool('b');
  v.email('e');
  assert.equal(v.errors.length, 3);
});

test('non-object bodies are rejected', () => {
  assert.throws(() => new Validator([1, 2]), ApiError);
  assert.throws(() => new Validator(undefined), ApiError);
  assert.throws(() => new Validator('x'), ApiError);
});

test('ids: allowed characters and Firestore-reserved names', () => {
  assert.equal(isSafeId('3f2a9c10-aaaa-bbbb-cccc-1234567890ab'), true);
  assert.equal(isSafeId('off:6001234567890'), true);
  for (const bad of ['', '.', '..', '__x__', 'a/b', 'a b', 'x'.repeat(129)]) assert.equal(isSafeId(bad), false, bad);
});

test('list items get indexed error paths', () => {
  const v = new Validator({ items: [{ reps: -1 }] });
  const items = v.list('items', { min: 1 });
  const c = v.child(items[0], 'items', 0);
  c.num('reps', { min: 0 });
  v.absorb(c);
  assert.equal(v.errors[0]?.field, 'items[0].reps');
});
