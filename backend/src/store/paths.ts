/**
 * Every Firestore path in one place (same collection names the Android app used, so existing data
 * keeps its shape). Never build path strings anywhere else.
 */
export const paths = {
  user: (uid: string) => `users/${uid}`,
  weightEntries: (uid: string) => `users/${uid}/weight_entries`,
  goals: (uid: string) => `users/${uid}/goals`,
  nutritionGoals: (uid: string) => `users/${uid}/nutrition_goals`,
  workoutGoals: (uid: string) => `users/${uid}/workout_goals`,
  checkpointSchedules: (uid: string) => `users/${uid}/checkpoint_schedules`,
  foodLogs: (uid: string) => `users/${uid}/food_logs`,
  workoutSessions: (uid: string) => `users/${uid}/workout_sessions`,
  createdPlans: (uid: string) => `users/${uid}/created_plans`,
  wallet: (uid: string) => `users/${uid}/wallet/balance`,
  claims: (uid: string) => `users/${uid}/reward_claims`,
  achievements: (uid: string) => `users/${uid}/user_achievements`,
  verifiedPlans: 'verified_workout_plans',
} as const;

export const doc = (collection: string, id: string): string => `${collection}/${id}`;

/** Stable ids for one-per-user records, matching the Android IdGenerator.userSingletonId format. */
export const singletonId = (prefix: string, uid: string): string => `${prefix}_${uid}`;
