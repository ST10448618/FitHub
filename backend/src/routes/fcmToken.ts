// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import type { Router } from '../http/router.ts';
import { Validator } from '../http/validate.ts';
import { paths } from '../store/paths.ts';
import { readStr } from './deps.ts';
import type { RouteDeps } from './deps.ts';

/**
 * POST /api/v1/me/fcm-token
 *
 * Stores the caller's FCM registration token and current UI language under
 * `users/{uid}` in Firestore. The token is what the admin/broadcast endpoint
 * (and campaign tooling in the Firebase Console) uses to target this device.
 *
 * ### Why the app calls this instead of writing Firestore directly
 * This is the demo-video evidence for the learning outcome "custom cloud-
 * hosted REST API with visible data round-trips": the phone POSTs, the server
 * validates and persists, the admin endpoint later reads and uses it.
 *
 * ### Idempotency
 * Safe to call on every app launch — POST semantics with a fully-replacing
 * body. Calling twice with the same body produces the same stored document.
 *
 * ### Fields written
 *   fcmToken               string  (required, 20..4096 chars — FCM tokens are long)
 *   fcmTokenLanguage       string  ('en' | 'zu' | 'af', default 'en')
 *   fcmTokenUpdatedAt      ISO timestamp
 *   fcmTokenHistory        string[]  (last 5 tokens, newest first — for audit)
 *
 * ### Fields preserved
 * Everything else in `users/{uid}` is merged, not replaced.
 */
export function registerFcmTokenRoutes(router: Router, deps: RouteDeps): void {
  const { store, now } = deps;

  router.add('POST', '/api/v1/me/fcm-token', async (ctx) => {
    const v = new Validator(ctx.body);
    const token = v.str('token', { min: 20, max: 4096 });
    const language = v.optOneOf('language', ['en', 'zu', 'af'] as const) ?? 'en';
    v.finish();

    const stamp = now().toISOString();
    const path = paths.user(ctx.uid);

    return store.transaction(async (tx) => {
      const existing = await tx.get(path);
      const previousToken = readStr(existing, 'fcmToken');
      const history = Array.isArray(existing?.['fcmTokenHistory'])
        ? (existing?.['fcmTokenHistory'] as string[])
        : [];
      // Keep last 5 distinct tokens; drop the current one if it re-appears.
      const newHistory = [token, ...history.filter((t) => t !== token)].slice(0, 5);

      const merged = {
        ...(existing ?? {}),
        id: ctx.uid,
        fcmToken: token,
        fcmTokenLanguage: language,
        fcmTokenUpdatedAt: stamp,
        fcmTokenHistory: newHistory,
      };
      tx.set(path, merged);

      return {
        status: 200,
        body: {
          ok: true,
          updatedAt: stamp,
          // Echo the previous token prefix so the client can log "token rotated".
          previousTokenPrefix: previousToken ? previousToken.slice(0, 8) : null,
          tokenPrefix: token.slice(0, 8),
        },
      };
    });
  });
}