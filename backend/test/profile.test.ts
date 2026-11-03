import { after, before, test } from 'node:test';
import assert from 'node:assert/strict';
import { profileBody, startTestApi } from './helpers.ts';
import type { TestApi } from './helpers.ts';

let api: TestApi;
before(async () => {
  api = await startTestApi();
});
after(() => api.close());

test('GET /me is 404 before a profile exists', async () => {
  const r = await api.call('GET', '/api/v1/me', { token: 'user:nobody' });
  assert.equal(r.status, 404);
});

test('PUT /me rejects blank name, bad age and bad height, listing every problem', async () => {
  const r = await api.call('PUT', '/api/v1/me', { body: profileBody({ firstName: '   ', age: 0, heightCm: 300 }) });
  assert.equal(r.status, 400);
  assert.equal(r.body.error.code, 'VALIDATION_FAILED');
  const fields = r.body.error.details.map((d: { field: string }) => d.field).sort();
  assert.deepEqual(fields, ['age', 'firstName', 'heightCm']);
});

test('PUT /me rejects non-numeric age and unknown enum values', async () => {
  const r = await api.call('PUT', '/api/v1/me', { body: profileBody({ age: '30', gender: 'ROBOT', activityLevel: 'LAZY' }) });
  assert.equal(r.status, 400);
  assert.equal(r.body.error.details.length, 3);
});

test('PUT /me stores a valid profile, trims the name and uses the token email', async () => {
  const r = await api.call('PUT', '/api/v1/me', {
    token: 'user:carol:carol@real.com',
    body: profileBody({ firstName: '  Carol ', email: 'spoof@evil.com' }),
  });
  assert.equal(r.status, 200);
  assert.equal(r.headers.get('x-applied'), 'true');
  assert.equal(r.body.id, 'carol');
  assert.equal(r.body.firstName, 'Carol');
  assert.equal(r.body.email, 'carol@real.com'); // token wins over the body
  assert.equal(r.body.onboardingComplete, false);
  const got = await api.call('GET', '/api/v1/me', { token: 'user:carol' });
  assert.equal(got.body.firstName, 'Carol');
});

test('users can only ever see their own profile', async () => {
  await api.call('PUT', '/api/v1/me', { token: 'user:dave', body: profileBody({ firstName: 'Dave' }) });
  const eve = await api.call('GET', '/api/v1/me', { token: 'user:eve' });
  assert.equal(eve.status, 404); // Eve cannot reach Dave's document; there is no way to name another uid
  const dave = await api.call('GET', '/api/v1/me', { token: 'user:dave' });
  assert.equal(dave.body.firstName, 'Dave');
});

test('stale PUT is ignored (last-write-wins) and the stored profile is returned', async () => {
  const t = 'user:frank';
  await api.call('PUT', '/api/v1/me', { token: t, body: profileBody({ firstName: 'New', updatedAt: '2026-10-30T09:30:00Z' }) });
  const stale = await api.call('PUT', '/api/v1/me', { token: t, body: profileBody({ firstName: 'Old', updatedAt: '2026-10-30T09:00:00Z' }) });
  assert.equal(stale.status, 200);
  assert.equal(stale.headers.get('x-applied'), 'false');
  assert.equal(stale.body.firstName, 'New');
  // replaying the exact same request is idempotent
  const replay = await api.call('PUT', '/api/v1/me', { token: t, body: profileBody({ firstName: 'New', updatedAt: '2026-10-30T09:30:00Z' }) });
  assert.equal(replay.headers.get('x-applied'), 'true');
});

test('editing a profile never changes weights or onboarding status', async () => {
  const t = 'user:gina';
  await api.call('POST', '/api/v1/me/onboarding', { token: t, body: { firstName: 'Gina', gender: 'FEMALE', age: 28, heightCm: 165, weightKg: 62, activityLevel: 'LIGHTLY_ACTIVE' } });
  const r = await api.call('PUT', '/api/v1/me', { token: t, body: profileBody({ firstName: 'Gina2', updatedAt: '2026-10-30T09:59:00Z' }) });
  assert.equal(r.body.startingWeightKg, 62);
  assert.equal(r.body.currentWeightKg, 62);
  assert.equal(r.body.onboardingComplete, true);
});

const onboard = (over: Record<string, unknown> = {}) => ({
  firstName: 'Alex', gender: 'MALE', age: 30, heightCm: 180, weightKg: 80, targetWeightKg: 74, activityLevel: 'SEDENTARY', localDate: '2026-10-30', ...over,
});

test('onboarding creates profile, weight, goal, nutrition, workout goals and schedule', async () => {
  const r = await api.call('POST', '/api/v1/me/onboarding', { token: 'user:henry:henry@x.com', body: onboard() });
  assert.equal(r.status, 200);
  const b = r.body;
  assert.equal(b.profile.onboardingComplete, true);
  assert.equal(b.profile.startingWeightKg, 80);
  assert.equal(b.profile.email, 'henry@x.com');
  assert.equal(b.weightEntry.weightKg, 80);
  assert.equal(b.weightEntry.date, '2026-10-30');
  assert.equal(b.goal.direction, 'LOSE'); // derived by the server, not chosen by the user
  assert.equal(b.recommendation.recommendedDailyCalories, 1886);
  assert.equal(b.nutritionGoals.userDailyCalories, 1886);
  assert.equal(b.nutritionGoals.recommendedDailyCalories, 1886);
  const m = b.nutritionGoals.mealTargets;
  assert.equal(m.breakfastKcal + m.lunchKcal + m.dinnerKcal + m.snackKcal, 1886);
  assert.equal(b.workoutGoals.sessionsPerWeek, 4);
  assert.equal(b.checkpointSchedule.frequencyDays, 14);
  assert.equal(b.checkpointSchedule.nextCheckpointDate, '2026-11-13');
  // really persisted in the database under the user's own path
  assert.ok(await api.store.get('users/henry'));
  assert.ok(await api.store.get('users/henry/goals/goal_henry'));
});

test('onboarding without a target weight means maintain', async () => {
  const r = await api.call('POST', '/api/v1/me/onboarding', { token: 'user:ivy', body: onboard({ targetWeightKg: undefined }) });
  assert.equal(r.body.goal.direction, 'MAINTAIN');
  assert.equal(r.body.recommendation.recommendedDailyCalories, 2136);
});

test('onboarding twice is idempotent and does not reset anything', async () => {
  const t = 'user:jack';
  const first = await api.call('POST', '/api/v1/me/onboarding', { token: t, body: onboard() });
  const second = await api.call('POST', '/api/v1/me/onboarding', { token: t, body: onboard({ weightKg: 120, firstName: 'Changed' }) });
  assert.equal(second.status, 200);
  assert.equal(second.body.profile.firstName, 'Alex');
  assert.equal(second.body.profile.startingWeightKg, 80);
  assert.deepEqual(second.body.nutritionGoals, first.body.nutritionGoals);
});

test('onboarding validates input and writes nothing when it is invalid', async () => {
  const r = await api.call('POST', '/api/v1/me/onboarding', { token: 'user:kim', body: onboard({ weightKg: 5, age: 200, firstName: '' }) });
  assert.equal(r.status, 400);
  assert.equal(r.body.error.details.length, 3);
  assert.equal(await api.store.get('users/kim'), null);
});
