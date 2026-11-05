// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { getMessaging } from 'firebase-admin/messaging';
import { ApiError } from '../http/errors.ts';
import type { Router } from '../http/router.ts';
import { Validator } from '../http/validate.ts';
import type { RouteDeps } from './deps.ts';

/**
 * POST /api/v1/admin/broadcast
 *
 * Sends an FCM notification to a topic. Admin-only.
 *
 * ### Auth
 * Firebase custom claim `admin === true` on the caller's ID token. The
 * `TokenVerifier` surfaces the claims through `Ctx.claims`; this route checks
 * `ctx.claims['admin'] === true` and 403s otherwise. There is no second
 * "password" — the claim is the only gate, set on the test account via
 * `backend/scripts/set-admin-claim.md`.
 *
 * ### Topics
 * Only the three topics the Android app subscribes to. Freeform topic names
 * would let an admin spray messages to any topic Firebase has ever seen,
 * including internal ones — the allowlist keeps blast radius bounded.
 *
 * ### Contract
 * Request body:
 *   { title: string (1..120), body: string (1..500), topic: 'fithub_all' | 'fithub_reminders' | 'fithub_streaks' }
 * Response (200):
 *   { ok: true, messageId: string, topic: string }
 *
 * ### Not in scope
 * Scheduling, deep links, per-user targeting, delivery receipts. FCM topic
 * send is fire-and-forget — `getMessaging().send()` resolves when the FCM
 * backend has accepted the message, not when devices have received it.
 *
 * Reference: https://firebase.google.com/docs/cloud-messaging/send-message
 * (accessed February 2026)
 */
const ALLOWED_TOPICS = ['fithub_all', 'fithub_reminders', 'fithub_streaks'] as const;
type AllowedTopic = (typeof ALLOWED_TOPICS)[number];

export function registerBroadcastRoutes(router: Router, _deps: RouteDeps): void {
  router.add('POST', '/api/v1/admin/broadcast', async (ctx) => {
    if (ctx.claims['admin'] !== true) {
      throw new ApiError(403, 'FORBIDDEN', 'Admin claim required.');
    }

    const v = new Validator(ctx.body);
    const title = v.str('title', { min: 1, max: 120 });
    const body = v.str('body', { min: 1, max: 500 });
    const topic: AllowedTopic = v.oneOf('topic', ALLOWED_TOPICS);
    v.finish();

    const messageId = await getMessaging().send({
      topic,
      notification: { title, body },
      android: { priority: 'high' },
      // Web/APNs not configured — this app is Android-only.
    });

    return {
      status: 200,
      body: { ok: true, messageId, topic },
    };
  });
}