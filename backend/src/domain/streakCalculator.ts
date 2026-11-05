// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
/**
 * Pure-TS port of the Android `StreakCalculator` (Kotlin, Phase 6 / UDF 5) so
 * `/api/v1/me/summary` reports the exact same streak value the app shows.
 *
 * ### Semantics (identical to the Kotlin version)
 *  • `current` = consecutive days ending today, or ending yesterday if today
 *    hasn't been logged yet. Yesterday-ending streaks are "at risk".
 *  • `todayDone` = today is in the qualifying-activity set.
 *  • `longest` = best-ever run in the entire input.
 *  • A single missed day resets the current streak.
 *
 * `today` is injected as `YYYY-MM-DD` so the calculator is time-zone-agnostic
 * and testable without a fake clock. The route calls it with the server's UTC
 * date; the app is UTC too, so the two agree end-to-end.
 *
 * Reference: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
 * (accessed February 2026)
 */
export interface StreakResult {
  current: number;
  longest: number;
  todayDone: boolean;
  atRisk: boolean;
}

/** YYYY-MM-DD, subtract `n` days, return YYYY-MM-DD. */
function minusDays(isoDate: string, n: number): string {
  const d = new Date(`${isoDate}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() - n);
  return d.toISOString().slice(0, 10);
}

/** Whole-day difference `b - a` (both YYYY-MM-DD). */
function daysBetween(a: string, b: string): number {
  const da = Date.parse(`${a}T00:00:00Z`);
  const db = Date.parse(`${b}T00:00:00Z`);
  return Math.round((db - da) / 86_400_000);
}

export function calculateStreak(activeDates: Set<string>, today: string): StreakResult {
  if (activeDates.size === 0) {
    return { current: 0, longest: 0, todayDone: false, atRisk: false };
  }

  const todayDone = activeDates.has(today);

  // Longest streak across the whole history.
  const sorted = [...activeDates].sort();
  let longest = 1;
  let run = 1;
  for (let i = 1; i < sorted.length; i++) {
    if (daysBetween(sorted[i - 1] as string, sorted[i] as string) === 1) {
      run++;
      if (run > longest) longest = run;
    } else {
      run = 1;
    }
  }

  // Current streak: walk backwards from either today or yesterday.
  const anchor = todayDone ? today : minusDays(today, 1);
  let current = 0;
  let cursor = anchor;
  while (activeDates.has(cursor)) {
    current++;
    cursor = minusDays(cursor, 1);
  }

  const atRisk = !todayDone && current >= 2;
  return {
    current,
    longest: Math.max(longest, current),
    todayDone,
    atRisk,
  };
}