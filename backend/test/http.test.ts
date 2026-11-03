import { after, before, test } from 'node:test';
import assert from 'node:assert/strict';
import { startTestApi } from './helpers.ts';
import type { TestApi } from './helpers.ts';

let api: TestApi;
before(async () => {
  api = await startTestApi({ maxBodyBytes: 2000 });
});
after(() => api.close());

test('GET /health is public', async () => {
  const r = await api.call('GET', '/health', { token: null });
  assert.equal(r.status, 200);
  assert.equal(r.body.status, 'ok');
});

test('protected routes reject missing, malformed and unknown tokens with the standard error shape', async () => {
  for (const token of [null, 'garbage', '']) {
    const r = await api.call('GET', '/api/v1/me', { token });
    assert.equal(r.status, 401, `token=${token}`);
    assert.equal(r.body.error.code, 'UNAUTHENTICATED');
    assert.ok(r.body.error.requestId);
  }
  const bad = await api.call('GET', '/api/v1/me', { headers: { Authorization: 'Basic abc' }, token: null });
  assert.equal(bad.status, 401);
});

test('unknown route is 404 and wrong method is 405 with an Allow header', async () => {
  const nf = await api.call('GET', '/api/v1/nope');
  assert.equal(nf.status, 404);
  assert.equal(nf.body.error.code, 'ROUTE_NOT_FOUND');
  const na = await api.call('DELETE', '/api/v1/me');
  assert.equal(na.status, 405);
  assert.match(na.headers.get('allow') ?? '', /GET/);
});

test('invalid JSON is 400, wrong content type is 415, oversized body is 413', async () => {
  const badJson = await api.call('PUT', '/api/v1/me', { rawBody: '{not json' });
  assert.equal(badJson.status, 400);
  assert.equal(badJson.body.error.code, 'INVALID_JSON');

  const wrongType = await api.call('PUT', '/api/v1/me', { rawBody: 'x=1', headers: { 'Content-Type': 'text/plain' } });
  assert.equal(wrongType.status, 415);

  const big = await api.call('PUT', '/api/v1/me', { rawBody: JSON.stringify({ pad: 'x'.repeat(5000) }) });
  assert.equal(big.status, 413);
});

test('every response carries a request id and no-store caching', async () => {
  const r = await api.call('GET', '/api/v1/me', { headers: { 'X-Request-Id': 'trace-12345678' } });
  assert.equal(r.headers.get('x-request-id'), 'trace-12345678');
  assert.equal(r.headers.get('cache-control'), 'no-store');
  assert.equal(r.headers.get('x-content-type-options'), 'nosniff');
});

test('rate limiting answers 429 with Retry-After', async () => {
  const limited = await startTestApi({ rateLimitPerMinute: 3 });
  try {
    const codes: number[] = [];
    for (let i = 0; i < 5; i++) codes.push((await limited.call('GET', '/api/v1/me')).status);
    assert.deepEqual(codes, [404, 404, 404, 429, 429]); // 404 = no profile yet, i.e. allowed through
    const r = await limited.call('GET', '/api/v1/me');
    assert.ok(Number(r.headers.get('retry-after')) >= 1);
    // a different user has their own allowance
    assert.equal((await limited.call('GET', '/api/v1/me', { token: 'user:bob' })).status, 404);
  } finally {
    await limited.close();
  }
});
