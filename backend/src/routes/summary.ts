// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import type { Router } from '../http/router.ts';
import { Validator } from '../http/validate.ts';
import { calculateStreak } from '../domain/streakCalculator.ts';
import { paths } from '../store/paths.ts';
import type { Doc } from '../store/store.ts';
import { minusDays, readNum, readStr, utcDate } from './deps.ts';
import type { RouteDeps } from './deps.ts';

/**
 * GET /api/v1/me/summary?days=N
 *
 * Aggregates the caller's own food logs, weight entries and workout sessions
 * over the last N days (default 7). Returns the exact shape documented in
 * `openapi.yaml` and consumed by `WeeklyInsightsCard` on the Android side.
 *
 * ### Why the aggregation happens here rather than on the phone
 * The demo video needs to show that "the server did the work" — the app can
 * call this endpoint and render the numbers without ever touching Room. The
 * phone still works offline via its own local aggregations (the Dashboard
 * cards), so there's no functional regression when the network drops.
 *
 * ### Streak
 * Uses the same `calculateStreak` algorithm as the app's `StreakCalculator`
 * so the two values agree end-to-end. Date is UTC for both sides.
 *
 * References:
 *  - https://firebase.google.com/docs/firestore/query-data/queries
 *  - https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Date
 * (accessed February 2026)
 */
export function registerSummaryRoutes(router: Router, deps: RouteDeps): void {
  const { store, now } = deps;

  router.add('GET', '/api/v1/me/summary', async (ctx) => {
    // Optional `days` query, 1..90, default 7.
    const daysParam = ctx.query.get('days');
    const v = new Validator(daysParam === null ? {} : { days: Number(daysParam) });
    const days = daysParam === null ? 7 : v.num('days', { int: true, min: 1, max: 90 });
    v.finish();

    const toDate = utcDate(now());
    const fromDate = minusDays(toDate, days - 1);

    // All three query sets filter on a single field (`logDate`) to stay
    // Firestore index-free, exactly as designed in `Store`.
    const [foodLogs, weightEntries, workoutSessions] = await Promise.all([
      store.list(paths.foodLogs(ctx.uid), {
        where: [{ field: 'logDate', op: '>=', value: fromDate }, { field: 'logDate', op: '<=', value: toDate }],
        sortBy: { field: 'logDate', direction: 'asc' },
      }),
      store.list(paths.weightEntries(ctx.uid), {
        where: [{ field: 'date', op: '>=', value: fromDate }, { field: 'date', op: '<=', value: toDate }],
        sortBy: { field: 'date', direction: 'asc' },
      }),
      store.list(paths.workoutSessions(ctx.uid), {
        where: [{ field: 'logDate', op: '>=', value: fromDate }, { field: 'logDate', op: '<=', value: toDate }],
        sortBy: { field: 'logDate', direction: 'asc' },
      }),
    ]);

    // Calories per day (only days that actually have logs are emitted, so the
    // client can plot without gaps → no fake zeroes).
    const caloriesByDay = new Map<string, number>();
    for (const log of foodLogs) {
      const date = readStr(log, 'logDate');
      const kcal = readNum(log, 'calories');
      if (date && kcal !== undefined) {
        caloriesByDay.set(date, (caloriesByDay.get(date) ?? 0) + kcal);
      }
    }
    const calories = [...caloriesByDay.entries()]
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([date, kcal]) => ({ date, kcal: Math.round(kcal) }));

    // Weight trend — every weigh-in inside the window.
    const weightTrend = weightEntries
      .map((w) => ({
        date: readStr(w, 'date') ?? '',
        weightKg: readNum(w, 'weightKg') ?? 0,
      }))
      .filter((w) => w.date !== '');

    // Sessions this week (Monday..Sunday, matching `WeeklyChallengeCalculator`).
    const monday = mondayOf(toDate);
    const sunday = addDays(monday, 6);
    const sessionsThisWeek = workoutSessions.filter((s) => {
      const d = readStr(s, 'logDate');
      return d !== undefined && d >= monday && d <= sunday && readStr(s, 'status') === 'COMPLETED';
    }).length;

    // Streak — union of any food-log or completed-session day, all-time.
    // The window above is only for the summary rows; the streak uses the full
    // history so a 30-day streak is reported even when `days=7`.
    const allFoodLogs = await store.list(paths.foodLogs(ctx.uid));
    const allSessions = await store.list(paths.workoutSessions(ctx.uid));
    const activeDates = new Set<string>();
    for (const l of allFoodLogs) {
      const d = readStr(l, 'logDate');
      if (d) activeDates.add(d);
    }
    for (const s of allSessions) {
      const d = readStr(s, 'logDate');
      if (d && readStr(s, 'status') === 'COMPLETED') activeDates.add(d);
    }
    const streak = calculateStreak(activeDates, toDate);

    return {
      body: {
        fromDate,
        toDate,
        calories,
        weightTrend,
        sessionsThisWeek,
        streak,
      },
    };
  });
}

function mondayOf(isoDate: string): string {
  const d = new Date(`${isoDate}T00:00:00Z`);
  // getUTCDay(): 0 = Sunday, 1 = Monday, …, 6 = Saturday.
  const dow = d.getUTCDay();
  const offset = dow === 0 ? 6 : dow - 1; // days back to Monday
  d.setUTCDate(d.getUTCDate() - offset);
  return d.toISOString().slice(0, 10);
}

function addDays(date: string, days: number): string {
  const d = new Date(`${date}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + days);
  return d.toISOString().slice(0, 10);
}