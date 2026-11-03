import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  calculateBmr,
  calculateCalories,
  goalDirection,
  mealTotal,
  recommend,
  starterMacros,
  starterMealTargets,
} from '../src/domain/calculators.ts';

test('BMR matches Mifflin-St Jeor for each gender', () => {
  // male: 10*80 + 6.25*180 - 5*30 + 5 = 1780
  assert.equal(calculateBmr(30, 'MALE', 180, 80), 1780);
  // female: 10*60 + 6.25*165 - 5*25 - 161 = 1345.25
  assert.equal(calculateBmr(25, 'FEMALE', 165, 60), 1345.25);
  // other uses the midpoint offset (-78)
  assert.equal(calculateBmr(30, 'OTHER', 180, 80), 1697); // 1775 - 78
});

test('maintenance, loss and gain adjust TDEE by 0, -250, +250', () => {
  const base = { age: 30, gender: 'MALE', heightCm: 180, weightKg: 80, activityLevel: 'SEDENTARY' } as const;
  assert.equal(calculateCalories({ ...base, goalDirection: 'MAINTAIN' }).recommendedDailyCalories, 2136); // 1780*1.2
  assert.equal(calculateCalories({ ...base, goalDirection: 'LOSE' }).recommendedDailyCalories, 1886);
  assert.equal(calculateCalories({ ...base, goalDirection: 'GAIN' }).recommendedDailyCalories, 2386);
});

test('fractions are truncated like Kotlin toInt()', () => {
  // 1345.25 * 1.375 = 1849.84375 ; lose => 1599.84375 -> 1599
  const out = calculateCalories({
    age: 25, gender: 'FEMALE', heightCm: 165, weightKg: 60, activityLevel: 'LIGHTLY_ACTIVE', goalDirection: 'LOSE',
  });
  assert.equal(out.recommendedDailyCalories, 1599);
  assert.equal(out.goalAdjustmentApplied, -250);
});

test('under-18s get no weight-change adjustment', () => {
  const out = calculateCalories({
    age: 16, gender: 'MALE', heightCm: 170, weightKg: 60, activityLevel: 'SEDENTARY', goalDirection: 'LOSE',
  });
  assert.equal(out.adultAdjustmentApplied, false);
  assert.equal(out.goalAdjustmentApplied, 0);
  assert.equal(out.recommendedDailyCalories, 1905); // (600+1062.5-80+5)*1.2
});

test('never recommends fewer than 1200 kcal', () => {
  const out = calculateCalories({
    age: 60, gender: 'FEMALE', heightCm: 150, weightKg: 40, activityLevel: 'SEDENTARY', goalDirection: 'LOSE',
  });
  assert.equal(out.recommendedDailyCalories, 1200);
});

test('starter macros are 20/50/30 percent of calories', () => {
  assert.deepEqual(starterMacros(2000), { proteinG: 100, carbsG: 250, fatG: 67 });
});

test('meal targets always add up to the daily total', () => {
  assert.deepEqual(starterMealTargets(2000), { breakfastKcal: 480, lunchKcal: 520, dinnerKcal: 600, snackKcal: 400 });
  for (const kcal of [1200, 1599, 1777, 2001, 2386, 3333]) {
    assert.equal(mealTotal(starterMealTargets(kcal)), kcal, `total for ${kcal}`);
  }
});

test('goal direction uses a 0.5 kg maintain tolerance', () => {
  assert.equal(goalDirection(80, 80.5), 'MAINTAIN');
  assert.equal(goalDirection(80, 79.5), 'MAINTAIN');
  assert.equal(goalDirection(80, 79.4), 'LOSE');
  assert.equal(goalDirection(80, 85), 'GAIN');
});

test('recommend() bundles calories, macros and meals consistently', () => {
  const r = recommend({
    age: 30, gender: 'MALE', heightCm: 180, weightKg: 80, activityLevel: 'SEDENTARY', goalDirection: 'LOSE',
  });
  assert.equal(r.recommendedDailyCalories, 1886);
  assert.equal(mealTotal(r.mealTargets), 1886);
  assert.equal(r.direction, 'LOSE');
});
