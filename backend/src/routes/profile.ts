// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { notFound } from '../http/errors.ts';
import type { Router } from '../http/router.ts';
import { Validator } from '../http/validate.ts';
import { ACTIVITY_LEVELS, GENDERS } from '../domain/types.ts';
import type { ActivityLevel, Gender } from '../domain/types.ts';
import { goalDirection, recommend } from '../domain/calculators.ts';
import { doc, paths, singletonId } from '../store/paths.ts';
import { resolveLww } from '../store/lww.ts';
import type { Doc } from '../store/store.ts';
import { readBool, readNum, readStr, utcDate } from './deps.ts';
import type { RouteDeps } from './deps.ts';

/**
 * Profile endpoints:
 *   GET  /api/v1/me             read the signed-in user's profile
 *   PUT  /api/v1/me             create / update it (validated, last-write-wins)
 *   POST /api/v1/me/onboarding  create profile + starting weight + goals in one transaction
 */
export function registerProfileRoutes(router: Router, deps: RouteDeps): void {
  const { store, now } = deps;

  router.add('GET', '/api/v1/me', async (ctx) => {
    const profile = await store.get(paths.user(ctx.uid));
    if (!profile) throw notFound('Profile');
    return { body: profile };
  });

  router.add('PUT', '/api/v1/me', async (ctx) => {
    const v = new Validator(ctx.body);
    const firstName = v.str('firstName', { min: 1, max: 50 });
    const surname = v.optStr('surname', { max: 50 }) ?? '';
    const bodyEmail = v.email('email');
    const gender = v.oneOf('gender', GENDERS);
    const age = v.num('age', { int: true, min: 1, max: 120 });
    const heightCm = v.num('heightCm', { min: 80, max: 250 });
    const activityLevel = v.oneOf('activityLevel', ACTIVITY_LEVELS);
    const profileImageUrl = v.optStr('profileImageUrl', { max: 2048 });
    const biometricEnabled = v.optBool('biometricEnabled');
    const updatedAt = v.timestamp('updatedAt');
    v.finish();

    return store.transaction(async (tx) => {
      const path = paths.user(ctx.uid);
      const existing = await tx.get(path);
      const lww = resolveLww(existing, updatedAt, now());
      if (!lww.apply && existing) return { body: existing, headers: { 'X-Applied': 'false' } };

      const created = readStr(existing, 'createdAt') ?? now().toISOString();
      const stored: Doc = {
        id: ctx.uid,
        firstName,
        surname,
        // The verified token's email is authoritative: the app cannot store a
        // different address than the one the account actually signs in with.
        email: ctx.email ?? bodyEmail ?? readStr(existing, 'email') ?? '',
        gender,
        age,
        heightCm,
        activityLevel,
        startingWeightKg: readNum(existing, 'startingWeightKg') ?? 0,
        currentWeightKg: readNum(existing, 'currentWeightKg') ?? 0,
        profileImageUrl: profileImageUrl === undefined ? (readStr(existing, 'profileImageUrl') ?? null) : profileImageUrl,
        biometricEnabled: biometricEnabled ?? readBool(existing, 'biometricEnabled') ?? false,
        onboardingComplete: readBool(existing, 'onboardingComplete') ?? false,
        createdAt: created,
        updatedAt: lww.updatedAt,
      };
      tx.set(path, stored);
      return { body: stored, headers: { 'X-Applied': 'true' } };
    });
  });

  router.add('POST', '/api/v1/me/onboarding', async (ctx) => {
    const v = new Validator(ctx.body);
    const firstName = v.str('firstName', { min: 1, max: 50 });
    const surname = v.optStr('surname', { max: 50 }) ?? '';
    const bodyEmail = v.email('email');
    const gender: Gender = v.oneOf('gender', GENDERS);
    const age = v.num('age', { int: true, min: 1, max: 120 });
    const heightCm = v.num('heightCm', { min: 80, max: 250 });
    const weightKg = v.num('weightKg', { min: 20, max: 400 });
    const targetWeightKg = v.optNum('targetWeightKg', { min: 20, max: 400 }) ?? weightKg;
    const activityLevel: ActivityLevel = v.oneOf('activityLevel', ACTIVITY_LEVELS);
    const biometricEnabled = v.optBool('biometricEnabled') ?? false;
    const localDate = v.optDate('localDate') ?? utcDate(now());
    v.finish();

    const uid = ctx.uid;
    return store.transaction(async (tx) => {
      const stamp = now().toISOString();

      // All reads first (Firestore requires reads before writes inside a transaction).
      const ids = {
        weight: singletonId('start', uid),
        goal: singletonId('goal', uid),
        nutrition: singletonId('nutrition_goals', uid),
        workout: singletonId('workout_goals', uid),
        checkpoint: singletonId('checkpoint', uid),
      };
      const [existingProfile, weight, goal, nutrition, workout, checkpoint] = await Promise.all([
        tx.get(paths.user(uid)),
        tx.get(doc(paths.weightEntries(uid), ids.weight)),
        tx.get(doc(paths.goals(uid), ids.goal)),
        tx.get(doc(paths.nutritionGoals(uid), ids.nutrition)),
        tx.get(doc(paths.workoutGoals(uid), ids.workout)),
        tx.get(doc(paths.checkpointSchedules(uid), ids.checkpoint)),
      ]);

      // Each record is "create if absent", so calling onboarding twice never
      // duplicates or resets anything.
      const profile: Doc =
        existingProfile && readBool(existingProfile, 'onboardingComplete')
          ? existingProfile
          : {
              id: uid,
              firstName,
              surname,
              email: ctx.email ?? bodyEmail ?? '',
              gender,
              age,
              heightCm,
              activityLevel,
              startingWeightKg: weightKg,
              currentWeightKg: weightKg,
              profileImageUrl: null,
              biometricEnabled,
              onboardingComplete: true,
              createdAt: readStr(existingProfile, 'createdAt') ?? stamp,
              updatedAt: stamp,
            };

      const pWeight = readNum(profile, 'currentWeightKg') ?? weightKg;
      const direction = goalDirection(pWeight, targetWeightKg);
      const rec = recommend({
        age: readNum(profile, 'age') ?? age,
        gender: (readStr(profile, 'gender') as Gender | undefined) ?? gender,
        heightCm: readNum(profile, 'heightCm') ?? heightCm,
        weightKg: pWeight,
        activityLevel: (readStr(profile, 'activityLevel') as ActivityLevel | undefined) ?? activityLevel,
        goalDirection: direction,
      });

      const weightEntry: Doc = weight ?? {
        id: ids.weight,
        weightKg,
        date: localDate,
        note: null,
        updatedAt: stamp,
      };
      const goalDoc: Doc = goal ?? {
        id: ids.goal,
        currentWeightKgAtGoalSet: weightKg,
        targetWeightKg,
        direction,
        effectiveDate: localDate,
        updatedAt: stamp,
      };
      const nutritionDoc: Doc = nutrition ?? {
        id: ids.nutrition,
        recommendedDailyCalories: rec.recommendedDailyCalories,
        userDailyCalories: rec.recommendedDailyCalories,
        mealTargets: rec.mealTargets,
        macroTargets: rec.macroTargets,
        effectiveDate: localDate,
        updatedAt: stamp,
      };
      const workoutDoc: Doc = workout ?? {
        id: ids.workout,
        sessionsPerWeek: 4,
        monthlyActivityGoalKcal: 10000,
        updatedAt: stamp,
      };
      const checkpointDoc: Doc = checkpoint ?? {
        id: ids.checkpoint,
        frequencyDays: 14,
        nextCheckpointDate: addDays(localDate, 14),
        remindersEnabled: true,
        updatedAt: stamp,
      };

      if (profile !== existingProfile) tx.set(paths.user(uid), profile);
      if (!weight) tx.set(doc(paths.weightEntries(uid), ids.weight), weightEntry);
      if (!goal) tx.set(doc(paths.goals(uid), ids.goal), goalDoc);
      if (!nutrition) tx.set(doc(paths.nutritionGoals(uid), ids.nutrition), nutritionDoc);
      if (!workout) tx.set(doc(paths.workoutGoals(uid), ids.workout), workoutDoc);
      if (!checkpoint) tx.set(doc(paths.checkpointSchedules(uid), ids.checkpoint), checkpointDoc);

      return {
        body: {
          profile,
          weightEntry,
          goal: goalDoc,
          nutritionGoals: nutritionDoc,
          workoutGoals: workoutDoc,
          checkpointSchedule: checkpointDoc,
          recommendation: rec,
        },
      };
    });
  });
}

/** Adds days to a YYYY-MM-DD date (UTC arithmetic, so no daylight-saving surprises). */
function addDays(date: string, days: number): string {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
}