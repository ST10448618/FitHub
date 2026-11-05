// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { startTestApi } from './helpers.ts';

test('GET /health returns 200 without auth', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/health', { token: null });
  assert.equal(res.status, 200);
  assert.equal(res.body.status, 'ok');
});

test('GET /api/v1/me requires auth (401 without token)', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me', { token: null });
  assert.equal(res.status, 401);
});

test('Unknown path returns 404 with standard error envelope', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/does-not-exist', { token: 'user:alice' });
  assert.equal(res.status, 404);
  assert.ok(res.body.error);
  assert.equal(res.body.error.code, 'ROUTE_NOT_FOUND');
  assert.ok(res.body.error.requestId);
});

test('Wrong method on an existing path returns 405 with Allow header', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('DELETE', '/api/v1/me', { token: 'user:alice' });
  assert.equal(res.status, 405);
  assert.match(res.headers.get('allow') ?? '', /GET|PUT/);
});

test('Malformed JSON body returns 400', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('PUT', '/api/v1/me', {
    token: 'user:alice',
    rawBody: '{ not json',
  });
  assert.equal(res.status, 400);
  assert.equal(res.body.error.code, 'INVALID_JSON');
});

test('Oversized body returns 413', async (t) => {
  const api = await startTestApi({ maxBodyBytes: 100 });
  t.after(() => api.close());
  const res = await api.call('PUT', '/api/v1/me', {
    token: 'user:alice',
    rawBody: JSON.stringify({ pad: 'x'.repeat(500) }),
  });
  assert.equal(res.status, 413);
});