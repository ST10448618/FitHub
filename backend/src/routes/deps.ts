// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import type { Store } from '../store/store.ts';
import type { Doc } from '../store/store.ts';

/** What every route module receives. `now` is injected so tests can control time. */
export interface RouteDeps {
  store: Store;
  now: () => Date;
}

/** Date part (YYYY-MM-DD) of a Date, in UTC. */
export const utcDate = (d: Date): string => d.toISOString().slice(0, 10);

/** Subtract `days` from a YYYY-MM-DD, return YYYY-MM-DD (UTC arithmetic). */
export const minusDays = (isoDate: string, days: number): string => {
  const d = new Date(`${isoDate}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() - days);
  return d.toISOString().slice(0, 10);
};

// Typed readers for stored documents (Doc values are `unknown`).
export const readStr = (d: Doc | null | undefined, k: string): string | undefined =>
  typeof d?.[k] === 'string' ? (d[k] as string) : undefined;
export const readNum = (d: Doc | null | undefined, k: string): number | undefined =>
  typeof d?.[k] === 'number' ? (d[k] as number) : undefined;
export const readBool = (d: Doc | null | undefined, k: string): boolean | undefined =>
  typeof d?.[k] === 'boolean' ? (d[k] as boolean) : undefined;