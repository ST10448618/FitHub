// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import type { Router } from '../http/router.ts';
import { registerProfileRoutes } from './profile.ts';
import { registerSummaryRoutes } from './summary.ts';
import { registerFcmTokenRoutes } from './fcmToken.ts';
import { registerFeedbackRoutes } from './feedback.ts';
import { registerBroadcastRoutes } from './broadcast.ts';
import type { RouteDeps } from './deps.ts';

/**
 * Registers every route module.
 * New resource modules are added here — `app.ts` never changes.
 */
export function registerRoutes(router: Router, deps: RouteDeps): void {
  registerProfileRoutes(router, deps);
  registerSummaryRoutes(router, deps);
  registerFcmTokenRoutes(router, deps);
  registerFeedbackRoutes(router, deps);
  registerBroadcastRoutes(router, deps);
}