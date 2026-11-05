// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { startTestApi, profileBody } from './helpers.ts';

const USER = 'user:alice:alice@example.com';

test('GET /me returns 404 before onboarding', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('GET', '/api/v1/me', { token: USER });
  assert.equal(res.status, 404);
});

test('PUT /me stores a profile, GET returns it back', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const put = await api.call('PUT', '/api/v1/me', {
    token: USER,
    body: profileBody(),
  });
  assert.equal(put.status, 200);
  assert.equal(put.body.firstName, 'Alex');
  assert.equal(put.body.email, 'alice@example.com'); // token wins over body
  assert.equal(put.headers.get('x-applied'), 'true');

  const get = await api.call('GET', '/api/v1/me', { token: USER });
  assert.equal(get.status, 200);
  assert.equal(get.body.firstName, 'Alex');
});

test('PUT /me rejects a missing required field', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('PUT', '/api/v1/me', {
    token: USER,
    body: { firstName: 'Alex' }, // missing gender, age, heightCm, activityLevel, updatedAt
  });
  assert.equal(res.status, 400);
  assert.equal(res.body.error.code, 'VALIDATION_FAILED');
  assert.ok(Array.isArray(res.body.error.details));
  assert.ok(res.body.error.details.length >= 3);
});

test('PUT /me is last-write-wins: older timestamp does not overwrite', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  // First write at 10:00
  await api.call('PUT', '/api/v1/me', {
    token: USER,
    body: profileBody({ firstName: 'Alex', updatedAt: '2026-10-30T10:00:00Z' }),
  });
  // Second write with an OLDER timestamp must be ignored
  const older = await api.call('PUT', '/api/v1/me', {
    token: USER,
    body: profileBody({ firstName: 'Bob', updatedAt: '2026-10-30T09:00:00Z' }),
  });
  assert.equal(older.status, 200);
  assert.equal(older.headers.get('x-applied'), 'false');
  assert.equal(older.body.firstName, 'Alex'); // original survived
});

test('POST /me/onboarding creates profile + weight + goals in one call', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const res = await api.call('POST', '/api/v1/me/onboarding', {
    token: USER,
    body: {
      firstName: 'Alex',
      surname: 'Hunter',
      email: 'alex@example.com',
      gender: 'MALE',
      age: 30,
      heightCm: 180,
      weightKg: 80,
      targetWeightKg: 75,
      activityLevel: 'MODERATELY_ACTIVE',
      localDate: '2026-10-30',
    },
  });
  assert.equal(res.status, 200);
  assert.equal(res.body.profile.onboardingComplete, true);
  assert.equal(res.body.goal.direction, 'LOSE');
  assert.ok(res.body.nutritionGoals.recommendedDailyCalories > 1200);
  assert.equal(res.body.workoutGoals.sessionsPerWeek, 4);
  assert.equal(res.body.checkpointSchedule.frequencyDays, 14);
});

test('POST /me/onboarding is idempotent (second call returns existing)', async (t) => {
  const api = await startTestApi();
  t.after(() => api.close());
  const body = {
    firstName: 'Alex',
    gender: 'MALE',
    age: 30,
    heightCm: 180,
    weightKg: 80,
    targetWeightKg: 75,
    activityLevel: 'MODERATELY_ACTIVE',
    localDate: '2026-10-30',
  };
  const first = await api.call('POST', '/api/v1/me/onboarding', { token: USER, body });
  const second = await api.call('POST', '/api/v1/me/onboarding', { token: USER, body });
  assert.equal(second.status, 200);
  assert.equal(second.body.profile.createdAt, first.body.profile.createdAt);
});