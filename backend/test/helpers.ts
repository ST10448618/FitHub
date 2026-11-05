// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { createServer } from 'node:http';
import type { Server } from 'node:http';
import type { AddressInfo } from 'node:net';
import { createApp } from '../src/app.ts';
import type { TokenVerifier, VerifiedUser } from '../src/auth/tokenVerifier.ts';
import { silentLogger } from '../src/http/logger.ts';
import { MemoryStore } from '../src/store/memoryStore.ts';

/**
 * Fake verifier. Accepted token shapes:
 *   "user:<uid>"                    → email = undefined, claims = {}
 *   "user:<uid>:<email>"            → email = <email>,  claims = {}
 *   "user:<uid>:<email>:admin"      → email = <email>,  claims = { admin: true }
 * Anything else is rejected (401 at the caller).
 */
export class FakeVerifier implements TokenVerifier {
  async verify(token: string): Promise<VerifiedUser | null> {
    const m = /^user:([^:]+)(?::([^:]+))?(?::admin)?$/.exec(token);
    if (!m) return null;
    const uid = m[1] as string;
    const email = m[2];
    const isAdmin = token.endsWith(':admin');
    return {
      uid,
      email,
      claims: isAdmin ? { admin: true } : {},
    };
  }
}

export interface TestApi {
  url: string;
  store: MemoryStore;
  setNow(iso: string): void;
  close(): Promise<void>;
  call(
    method: string,
    path: string,
    opts?: {
      token?: string | null;
      body?: unknown;
      rawBody?: string;
      headers?: Record<string, string>;
    },
  ): Promise<{ status: number; body: any; headers: Headers }>;
}

export async function startTestApi(
  overrides: { rateLimitPerMinute?: number; maxBodyBytes?: number } = {},
): Promise<TestApi> {
  const store = new MemoryStore();
  let nowIso = '2026-10-30T10:00:00.000Z';
  const app = createApp({
    verifier: new FakeVerifier(),
    store,
    logger: silentLogger,
    rateLimitPerMinute: overrides.rateLimitPerMinute ?? 1000,
    maxBodyBytes: overrides.maxBodyBytes ?? 100 * 1024,
    now: () => new Date(nowIso),
  });
  const server: Server = createServer(app);
  await new Promise<void>((resolve) => server.listen(0, '127.0.0.1', resolve));
  const url = `http://127.0.0.1:${(server.address() as AddressInfo).port}`;

  return {
    url,
    store,
    setNow: (iso) => {
      nowIso = iso;
    },
    close: () =>
      new Promise<void>((resolve) => {
        server.closeAllConnections();
        server.close(() => resolve());
      }),
    async call(method, path, opts = {}) {
      const headers: Record<string, string> = { ...opts.headers };
      if (opts.token !== null) headers.Authorization = `Bearer ${opts.token ?? 'user:alice'}`;
      let body: string | undefined;
      if (opts.rawBody !== undefined) {
        body = opts.rawBody;
        headers['Content-Type'] ??= 'application/json';
      } else if (opts.body !== undefined) {
        body = JSON.stringify(opts.body);
        headers['Content-Type'] = 'application/json';
      }
      const res = await fetch(url + path, { method, headers, body });
      const text = await res.text();
      return { status: res.status, body: text ? JSON.parse(text) : undefined, headers: res.headers };
    },
  };
}

/** A valid profile body; override fields per test. */
export const profileBody = (over: Record<string, unknown> = {}) => ({
  firstName: 'Alex',
  surname: 'Hunter',
  email: 'alex@example.com',
  gender: 'MALE',
  age: 30,
  heightCm: 180,
  activityLevel: 'SEDENTARY',
  updatedAt: '2026-10-30T09:00:00Z',
  ...over,
});