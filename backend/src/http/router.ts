/**
 * A tiny router: maps METHOD + path pattern to a handler.
 *   router.add('PUT', '/api/v1/weight-entries/:id', handler)
 * When several patterns match, the one with the fewest ":param" segments wins, so
 * /workout-plans/verified beats /workout-plans/:id.
 */
export interface Result {
  status?: number;
  body?: unknown;
  headers?: Record<string, string>;
}

export interface Ctx {
  /** Verified Firebase user id. Always taken from the token, never from the request. */
  uid: string;
  /** Email from the verified token, when present. */
  email: string | undefined;
  params: Record<string, string>;
  query: URLSearchParams;
  /** Parsed JSON body (undefined when the request had none). */
  body: unknown;
  requestId: string;
}

export type Handler = (ctx: Ctx) => Promise<Result>;

interface Route {
  method: string;
  segments: string[];
  handler: Handler;
}

export type MatchResult =
  | { kind: 'found'; handler: Handler; params: Record<string, string> }
  | { kind: 'method-not-allowed'; allowed: string[] }
  | { kind: 'not-found' };

export class Router {
  private readonly routes: Route[] = [];

  add(method: string, path: string, handler: Handler): void {
    this.routes.push({ method: method.toUpperCase(), segments: path.split('/').filter(Boolean), handler });
  }

  match(method: string, pathname: string): MatchResult {
    const parts = pathname.split('/').filter(Boolean);
    let best: { route: Route; params: Record<string, string>; dynamic: number } | undefined;
    const allowed = new Set<string>();

    for (const route of this.routes) {
      if (route.segments.length !== parts.length) continue;
      const params: Record<string, string> = {};
      let dynamic = 0;
      let ok = true;
      for (let i = 0; i < parts.length; i++) {
        const seg = route.segments[i] as string;
        const part = parts[i] as string;
        if (seg.startsWith(':')) {
          let decoded: string;
          try {
            decoded = decodeURIComponent(part);
          } catch {
            ok = false;
            break;
          }
          params[seg.slice(1)] = decoded;
          dynamic++;
        } else if (seg !== part) {
          ok = false;
          break;
        }
      }
      if (!ok) continue;
      allowed.add(route.method);
      if (route.method === method.toUpperCase() && (!best || dynamic < best.dynamic)) {
        best = { route, params, dynamic };
      }
    }

    if (best) return { kind: 'found', handler: best.route.handler, params: best.params };
    if (allowed.size > 0) return { kind: 'method-not-allowed', allowed: [...allowed].sort() };
    return { kind: 'not-found' };
  }
}
