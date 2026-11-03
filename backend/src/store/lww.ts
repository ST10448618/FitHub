import type { Doc } from './store.ts';

/**
 * Last-write-wins on "updatedAt".
 *
 * The app stamps each change with the time the user made it (important for offline edits that sync
 * later). When a write arrives:
 *   - if the stored copy is NEWER, the write is stale and is ignored (applied = false);
 *   - otherwise it is stored. Equal timestamps are applied, so retrying the same request is harmless.
 * A phone with a wrong clock cannot "win forever": timestamps more than 5 minutes ahead of the
 * server are clamped to server time + 5 minutes.
 */
const MAX_CLOCK_SKEW_MS = 5 * 60 * 1000;

export function resolveLww(
  existing: Doc | null,
  incomingUpdatedAt: string,
  now: Date,
): { apply: boolean; updatedAt: string } {
  const ceiling = now.getTime() + MAX_CLOCK_SKEW_MS;
  const incoming = Math.min(Date.parse(incomingUpdatedAt), ceiling);
  const updatedAt = new Date(incoming).toISOString();
  if (existing && typeof existing.updatedAt === 'string' && Date.parse(existing.updatedAt) > incoming) {
    return { apply: false, updatedAt };
  }
  return { apply: true, updatedAt };
}
