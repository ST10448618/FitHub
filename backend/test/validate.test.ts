// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { Validator } from '../src/http/validate.ts';
import { ApiError } from '../src/http/errors.ts';

test('str: required field missing throws', () => {
  const v = new Validator({});
  v.str('name', { min: 1, max: 50 });
  assert.throws(() => v.finish(), ApiError);
});

test('str: min length enforced', () => {
  const v = new Validator({ name: '' });
  v.str('name', { min: 1, max: 50 });
  assert.throws(() => v.finish(), ApiError);
});

test('str: valid string passes', () => {
  const v = new Validator({ name: 'Alex' });
  const s = v.str('name', { min: 1, max: 50 });
  v.finish();
  assert.equal(s, 'Alex');
});

test('num: integer enforcement rejects 1.5', () => {
  const v = new Validator({ age: 1.5 });
  v.num('age', { int: true, min: 1, max: 120 });
  assert.throws(() => v.finish(), ApiError);
});

test('num: range enforced', () => {
  const v = new Validator({ age: 200 });
  v.num('age', { int: true, min: 1, max: 120 });
  assert.throws(() => v.finish(), ApiError);
});

test('oneOf: rejects a value not in the allowlist', () => {
  const v = new Validator({ gender: 'ROBOT' });
  v.oneOf('gender', ['MALE', 'FEMALE', 'OTHER'] as const);
  assert.throws(() => v.finish(), ApiError);
});

test('oneOf: accepts a valid value', () => {
  const v = new Validator({ gender: 'MALE' });
  const g = v.oneOf('gender', ['MALE', 'FEMALE', 'OTHER'] as const);
  v.finish();
  assert.equal(g, 'MALE');
});

test('timestamp: requires ISO-8601 with offset', () => {
  const v = new Validator({ updatedAt: '2026-10-30 10:00:00' });
  v.timestamp('updatedAt');
  assert.throws(() => v.finish(), ApiError);
});

test('timestamp: accepts Z offset and normalises to millisecond precision', () => {
  const v = new Validator({ updatedAt: '2026-10-30T10:00:00Z' });
  const t = v.timestamp('updatedAt');
  v.finish();
  // Validator.timestamp returns Date#toISOString() — same instant, always ".000Z".
  assert.equal(new Date(t).toISOString(), '2026-10-30T10:00:00.000Z');
});