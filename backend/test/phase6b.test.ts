// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { startTestApi } from './helpers.ts';

const USER = 'user:u1:u1@example.com';
const ADMIN = 'user:admin:admin@example.com:admin';

// ---------- GET /api/v1/me/summary ----------

test('GET /me/summary returns zeroed aggregates for a fresh user', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me/summary?days=7', { token: USER });
  assert.equal(res.status, 200);
  assert.equal(res.body.calories.length, 0);
  assert.equal(res.body.weightTrend.length, 0);
  assert.equal(res.body.sessionsThisWeek, 0);
  assert.equal(res.body.streak.current, 0);
  assert.equal(res.body.streak.todayDone, false);
  assert.ok(res.body.fromDate);
  assert.ok(res.body.toDate);
});

test('GET /me/summary defaults to 7 days', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me/summary', { token: USER });
  assert.equal(res.status, 200);
  // 2026-10-30 today → 2026-10-24 fromDate
  assert.equal(res.body.toDate, '2026-10-30');
  assert.equal(res.body.fromDate, '2026-10-24');
});

test('GET /me/summary rejects days=0', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me/summary?days=0', { token: USER });
  assert.equal(res.status, 400);
  assert.equal(res.body.error.code, 'VALIDATION_FAILED');
});

test('GET /me/summary rejects days=999', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me/summary?days=999', { token: USER });
  assert.equal(res.status, 400);
});

test('GET /me/summary requires auth', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me/summary', { token: null });
  assert.equal(res.status, 401);
});

// ---------- POST /api/v1/me/fcm-token ----------

test('POST /me/fcm-token stores a token', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/fcm-token', {
    token: USER,
    body: { token: 'x'.repeat(40), language: 'zu' },
  });
  assert.equal(res.status, 200);
  assert.equal(res.body.ok, true);
  assert.match(res.body.tokenPrefix, /^xxxxxxxx$/);
  assert.equal(res.body.previousTokenPrefix, null);
});

test('POST /me/fcm-token rejects a short token', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/fcm-token', {
    token: USER,
    body: { token: 'short' },
  });
  assert.equal(res.status, 400);
});

test('POST /me/fcm-token records token rotation', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const first = await api.call('POST', '/api/v1/me/fcm-token', {
    token: USER,
    body: { token: 'a'.repeat(40) },
  });
  assert.equal(first.body.previousTokenPrefix, null);
  const second = await api.call('POST', '/api/v1/me/fcm-token', {
    token: USER,
    body: { token: 'b'.repeat(40) },
  });
  assert.equal(second.body.previousTokenPrefix, 'aaaaaaaa'); // first 8 of old
});

// ---------- POST /api/v1/me/feedback ----------

test('POST /me/feedback creates a document', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/feedback', {
    token: USER,
    body: { rating: 4, message: 'Loving the streak feature' },
  });
  assert.equal(res.status, 201);
  assert.match(res.body.id, /^fb_/);
  assert.ok(res.body.createdAt);
});

test('POST /me/feedback rejects rating > 5', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/feedback', {
    token: USER,
    body: { rating: 6 },
  });
  assert.equal(res.status, 400);
});

test('POST /me/feedback rejects rating = 0', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/feedback', {
    token: USER,
    body: { rating: 0 },
  });
  assert.equal(res.status, 400);
});

// ---------- POST /api/v1/admin/broadcast ----------

test('POST /admin/broadcast returns 403 without admin claim', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/admin/broadcast', {
    token: USER, // NOT the :admin one
    body: { title: 'T', body: 'B', topic: 'fithub_all' },
  });
  assert.equal(res.status, 403);
  assert.equal(res.body.error.code, 'FORBIDDEN');
});

test('POST /admin/broadcast validates the topic allowlist', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/admin/broadcast', {
    token: ADMIN,
    body: { title: 'T', body: 'B', topic: 'not-a-real-topic' },
  });
  assert.equal(res.status, 400);
  assert.equal(res.body.error.code, 'VALIDATION_FAILED');
});

// Note: the 200-success path for /admin/broadcast is NOT tested here because
// it requires a live Firebase Messaging project. It's covered manually in
// the POE demo video against the deployed Render instance.