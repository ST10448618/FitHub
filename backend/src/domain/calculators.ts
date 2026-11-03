import type { ActivityLevel, Gender, GoalDirection, MacroTargets, MealTargets } from './types.ts';

/**
 * Calorie, macro and goal-direction maths. A direct port of the Android calculators
 * (CalorieEngine, MacroCalculator, GoalDirectionCalculator, CalculatorConfig) so the server
 * gives the same answers the app used to compute on the phone. Moving it here is what makes the API
 * do the "heavy lifting".
 *
 * Formula reference: Mifflin MD, St Jeor ST, et al. (1990) "A new predictive equation for resting
 * energy expenditure in healthy individuals", Am J Clin Nutr 51(2):241-247.
 * These are prototype estimates, not medical advice.
 */
export const CONFIG = {
  WEIGHT_FACTOR: 10.0,
  HEIGHT_FACTOR: 6.25,
  AGE_FACTOR: 5.0,
  MALE_OFFSET: 5.0,
  FEMALE_OFFSET: -161.0,
  OTHER_OFFSET: -78.0, // midpoint of male and female
  GOAL_ADJUST_LOSE: -250.0,
  GOAL_ADJUST_MAINTAIN: 0.0,
  GOAL_ADJUST_GAIN: 250.0,
  MAINTAIN_TOLERANCE_KG: 0.5,
  ADULT_MIN_AGE: 18,
  MACRO_PROTEIN_FRACTION: 0.2,
  MACRO_CARB_FRACTION: 0.5,
  MACRO_FAT_FRACTION: 0.3,
  KCAL_PER_G_PROTEIN: 4.0,
  KCAL_PER_G_CARB: 4.0,
  KCAL_PER_G_FAT: 9.0,
  MEAL_BREAKFAST_FRACTION: 0.24,
  MEAL_LUNCH_FRACTION: 0.26,
  MEAL_DINNER_FRACTION: 0.3,
  MIN_RECOMMENDED_CALORIES: 1200.0,
} as const;

export const ACTIVITY_MULTIPLIER: Record<ActivityLevel, number> = {
  SEDENTARY: 1.2,
  LIGHTLY_ACTIVE: 1.375,
  MODERATELY_ACTIVE: 1.55,
  VERY_ACTIVE: 1.725,
  EXTREMELY_ACTIVE: 1.9,
};

export interface CalorieInput {
  age: number;
  gender: Gender;
  heightCm: number;
  weightKg: number;
  activityLevel: ActivityLevel;
  goalDirection: GoalDirection;
}

export interface CalorieOutput {
  bmrKcal: number;
  tdeeKcal: number;
  recommendedDailyCalories: number;
  goalAdjustmentApplied: number;
  adultAdjustmentApplied: boolean;
}

/** Mifflin-St Jeor basal metabolic rate (kcal/day at rest). */
export function calculateBmr(age: number, gender: Gender, heightCm: number, weightKg: number): number {
  const base = CONFIG.WEIGHT_FACTOR * weightKg + CONFIG.HEIGHT_FACTOR * heightCm - CONFIG.AGE_FACTOR * age;
  switch (gender) {
    case 'MALE':
      return base + CONFIG.MALE_OFFSET;
    case 'FEMALE':
      return base + CONFIG.FEMALE_OFFSET;
    default:
      return base + CONFIG.OTHER_OFFSET;
  }
}

/**
 * Recommended daily calories = TDEE (BMR x activity) plus a goal adjustment.
 * Under-18s only get the maintenance estimate (no weight-change adjustment).
 * The result is never lower than 1200 kcal. Fractions are truncated, as Kotlin's toInt() does.
 */
export function calculateCalories(input: CalorieInput): CalorieOutput {
  const bmr = calculateBmr(input.age, input.gender, input.heightCm, input.weightKg);
  const tdee = bmr * ACTIVITY_MULTIPLIER[input.activityLevel];
  const isAdult = input.age >= CONFIG.ADULT_MIN_AGE;

  let adjustment = 0;
  if (isAdult) {
    if (input.goalDirection === 'LOSE') adjustment = CONFIG.GOAL_ADJUST_LOSE;
    else if (input.goalDirection === 'GAIN') adjustment = CONFIG.GOAL_ADJUST_GAIN;
    else adjustment = CONFIG.GOAL_ADJUST_MAINTAIN;
  }

  const safe = Math.max(tdee + adjustment, CONFIG.MIN_RECOMMENDED_CALORIES);
  return {
    bmrKcal: bmr,
    tdeeKcal: tdee,
    recommendedDailyCalories: Math.trunc(safe),
    goalAdjustmentApplied: Math.trunc(adjustment),
    adultAdjustmentApplied: isAdult,
  };
}

/** Starter macro split: 20% protein, 50% carbs, 30% fat of the daily calories, in grams. */
export function starterMacros(dailyCalories: number): MacroTargets {
  return {
    proteinG: Math.round((dailyCalories * CONFIG.MACRO_PROTEIN_FRACTION) / CONFIG.KCAL_PER_G_PROTEIN),
    carbsG: Math.round((dailyCalories * CONFIG.MACRO_CARB_FRACTION) / CONFIG.KCAL_PER_G_CARB),
    fatG: Math.round((dailyCalories * CONFIG.MACRO_FAT_FRACTION) / CONFIG.KCAL_PER_G_FAT),
  };
}

/** Default meal split 24/26/30/20 %. The snack absorbs rounding so the total always matches. */
export function starterMealTargets(dailyCalories: number): MealTargets {
  const breakfastKcal = Math.round(dailyCalories * CONFIG.MEAL_BREAKFAST_FRACTION);
  const lunchKcal = Math.round(dailyCalories * CONFIG.MEAL_LUNCH_FRACTION);
  const dinnerKcal = Math.round(dailyCalories * CONFIG.MEAL_DINNER_FRACTION);
  return { breakfastKcal, lunchKcal, dinnerKcal, snackKcal: dailyCalories - breakfastKcal - lunchKcal - dinnerKcal };
}

export const mealTotal = (m: MealTargets): number => m.breakfastKcal + m.lunchKcal + m.dinnerKcal + m.snackKcal;

/** Direction is derived from current vs target weight; the user never picks it. */
export function goalDirection(currentWeightKg: number, targetWeightKg: number): GoalDirection {
  const delta = targetWeightKg - currentWeightKg;
  if (Math.abs(delta) <= CONFIG.MAINTAIN_TOLERANCE_KG) return 'MAINTAIN';
  return delta < 0 ? 'LOSE' : 'GAIN';
}

/** Full recommendation bundle used by onboarding and GET /nutrition-goals/recommendation. */
export function recommend(input: CalorieInput) {
  const cal = calculateCalories(input);
  return {
    bmrKcal: cal.bmrKcal,
    tdeeKcal: cal.tdeeKcal,
    recommendedDailyCalories: cal.recommendedDailyCalories,
    goalAdjustmentApplied: cal.goalAdjustmentApplied,
    direction: input.goalDirection,
    macroTargets: starterMacros(cal.recommendedDailyCalories),
    mealTargets: starterMealTargets(cal.recommendedDailyCalories),
  };
}
