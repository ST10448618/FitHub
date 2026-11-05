// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import type { Router } from '../http/router.ts';
import { Validator } from '../http/validate.ts';
import { paths } from '../store/paths.ts';
import { readNum, readStr } from './deps.ts';
import type { RouteDeps } from './deps.ts';

/**
 * POST /api/v1/me/feedback
 *
 * Stores a 1–5 star rating plus optional free-text comment under
 * `feedback/{autoId}` (top-level collection, not user-scoped — so an admin
 * listing feedback across all users needs only one query).
 *
 * ### Fields written
 *   uid         string  (from the verified token, never the body)
 *   email       string | null  (from the verified token)
 *   rating      integer  1..5
 *   message     string   0..2000 chars
 *   appVersion  string | null  (optional — from body, helps triage)
 *   createdAt   ISO timestamp
 *
 * ### Not in scope
 * Rate limiting, de-duplication, spam checks, export. The `RateLimiter` in
 * `http/rateLimit.ts` already caps requests per uid globally, which is
 * sufficient for a POE demo. Real products would add a per-endpoint cap.
 */
export function registerFeedbackRoutes(router: Router, deps: RouteDeps): void {
  const { store, now } = deps;

  router.add('POST', '/api/v1/me/feedback', async (ctx) => {
    const v = new Validator(ctx.body);
    const rating = v.num('rating', { int: true, min: 1, max: 5 });
    const message = v.optStr('message', { max: 2000 }) ?? '';
    const appVersion = v.optStr('appVersion', { max: 32 });
    v.finish();

    const stamp = now().toISOString();
    // Deterministic-ish id: timestamp + uid prefix. Avoids needing a UUID
    // dependency (the project only depends on firebase-admin).
    const id = `fb_${Date.now()}_${ctx.uid.slice(0, 8)}`;
    const record = {
      id,
      uid: ctx.uid,
      email: ctx.email ?? null,
      rating,
      message,
      appVersion: appVersion ?? null,
      createdAt: stamp,
    };
    await store.set(`${paths.feedback()}/${id}`, record);

    return {
      status: 201,
      body: {
        ok: true,
        id,
        createdAt: stamp,
      },
    };
  });
}