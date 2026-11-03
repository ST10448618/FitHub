import type { Router } from '../http/router.ts';
import { registerProfileRoutes } from './profile.ts';
import type { RouteDeps } from './deps.ts';

/** Registers every route module. New resource modules are added here. */
export function registerRoutes(router: Router, deps: RouteDeps): void {
  registerProfileRoutes(router, deps);
}
