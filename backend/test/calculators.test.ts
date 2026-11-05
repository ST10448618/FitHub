// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  calculateBmr,
  calculateCalories,
  goalDirection,
  starterMacros,
  starterMealTargets,
  mealTotal,
} from '../src/domain/calculators.ts';

test('BMR matches Mifflin-St Jeor for male', () => {
  // 10*80 + 6.25*180 - 5*30 + 5 = 800 + 1125 - 150 + 5 = 1780
  const bmr = calculateBmr(30, 'MALE', 180, 80);
  assert.equal(bmr, 1780);
});

test('BMR matches Mifflin-St Jeor for female', () => {
  // 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
  const bmr = calculateBmr(25, 'FEMALE', 165, 60);
  assert.equal(bmr, 1345.25);
});

test('BMR uses neutral offset for OTHER', () => {
  // 10*70 + 6.25*170 - 5*30 - 78 = 700 + 1062.5 - 150 - 78 = 1534.5
  const bmr = calculateBmr(30, 'OTHER', 170, 70);
  assert.equal(bmr, 1534.5);
});

test('recommended calories apply a +250 kcal gain adjustment for adults', () => {
  const out = calculateCalories({
    age: 30,
    gender: 'MALE',
    heightCm: 180,
    weightKg: 80,
    activityLevel: 'SEDENTARY',
    goalDirection: 'GAIN',
  });
  // TDEE = 1780 * 1.2 = 2136; +250 = 2386
  assert.equal(out.recommendedDailyCalories, 2386);
  assert.equal(out.goalAdjustmentApplied, 250);
  assert.equal(out.adultAdjustmentApplied, true);
});

test('recommended calories apply a -250 kcal lose adjustment', () => {
  const out = calculateCalories({
    age: 30,
    gender: 'MALE',
    heightCm: 180,
    weightKg: 80,
    activityLevel: 'MODERATELY_ACTIVE',
    goalDirection: 'LOSE',
  });
  // TDEE = 1780 * 1.55 = 2759; -250 = 2509
  assert.equal(out.recommendedDailyCalories, 2509);
  assert.equal(out.goalAdjustmentApplied, -250);
});

test('under-18s get maintenance only, no gain/lose adjustment', () => {
  const out = calculateCalories({
    age: 16,
    gender: 'MALE',
    heightCm: 175,
    weightKg: 65,
    activityLevel: 'SEDENTARY',
    goalDirection: 'LOSE',
  });
  assert.equal(out.goalAdjustmentApplied, 0);
  assert.equal(out.adultAdjustmentApplied, false);
});

test('starterMacros splits 20/50/30 in grams', () => {
  // 2000 kcal → protein 100 g, carbs 250 g, fat 66.67 ≈ 67 g
  const m = starterMacros(2000);
  assert.equal(m.proteinG, 100);
  assert.equal(m.carbsG, 250);
  assert.equal(m.fatG, 67);
});

test('starterMealTargets sum back to daily calories', () => {
  const m = starterMealTargets(2000);
  assert.equal(mealTotal(m), 2000);
  // 24/26/30 → breakfast 480, lunch 520, dinner 600, snack 400
  assert.equal(m.breakfastKcal, 480);
  assert.equal(m.lunchKcal, 520);
  assert.equal(m.dinnerKcal, 600);
  assert.equal(m.snackKcal, 400);
});

test('goalDirection uses LOSE below, GAIN above, MAINTAIN within 0.5 kg', () => {
  assert.equal(goalDirection(80, 75), 'LOSE');
  assert.equal(goalDirection(75, 80), 'GAIN');
  assert.equal(goalDirection(80, 80), 'MAINTAIN');
  assert.equal(goalDirection(80, 80.4), 'MAINTAIN');
  assert.equal(goalDirection(80, 79.6), 'MAINTAIN');
  assert.equal(goalDirection(80, 79.4), 'LOSE');
  assert.equal(goalDirection(80, 80.6), 'GAIN');
});