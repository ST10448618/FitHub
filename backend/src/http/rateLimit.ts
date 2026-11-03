/**
 * Fixed-window rate limiter, one counter per key (user id, or IP when not signed in).
 * In-memory, so it protects a single server instance. Fine for this project; a multi-instance
 * deployment would move the counters to Redis.
 */
export class RateLimiter {
  private readonly windows = new Map<string, { start: number; count: number }>();
  private readonly limit: number;
  private readonly windowMs: number;
  private readonly now: () => number;

  constructor(limit: number, windowMs = 60_000, now: () => number = Date.now) {
    this.limit = limit;
    this.windowMs = windowMs;
    this.now = now;
  }

  /** Records one request. Returns { ok:false, retryAfterSec } when the key is over its limit. */
  hit(key: string): { ok: true } | { ok: false; retryAfterSec: number } {
    const t = this.now();
    if (this.windows.size > 10_000) this.prune(t);
    const w = this.windows.get(key);
    if (!w || t - w.start >= this.windowMs) {
      this.windows.set(key, { start: t, count: 1 });
      return { ok: true };
    }
    w.count += 1;
    if (w.count > this.limit) {
      return { ok: false, retryAfterSec: Math.max(1, Math.ceil((w.start + this.windowMs - t) / 1000)) };
    }
    return { ok: true };
  }

  private prune(t: number): void {
    for (const [k, w] of this.windows) if (t - w.start >= this.windowMs) this.windows.delete(k);
  }
}
