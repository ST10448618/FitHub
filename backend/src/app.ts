import { randomUUID } from 'node:crypto';
import type { IncomingMessage, RequestListener, ServerResponse } from 'node:http';
import type { TokenVerifier } from './auth/tokenVerifier.ts';
import { ApiError, unauthenticated } from './http/errors.ts';
import type { Logger } from './http/logger.ts';
import { RateLimiter } from './http/rateLimit.ts';
import { Router } from './http/router.ts';
import type { Result } from './http/router.ts';
import { isSafeId } from './http/validate.ts';
import { registerRoutes } from './routes/index.ts';
import type { Store } from './store/store.ts';

export interface AppDeps {
  verifier: TokenVerifier;
  store: Store;
  logger: Logger;
  rateLimitPerMinute: number;
  maxBodyBytes: number;
  now?: () => Date;
}

const BODY_METHODS = new Set(['POST', 'PUT', 'PATCH']);

/**
 * Builds the request listener for http.createServer().
 *
 * Request pipeline:
 *   1. assign a request id            5. read + parse the JSON body (size-limited)
 *   2. find the route (404 / 405)     6. run the handler
 *   3. verify the Firebase token      7. send JSON (or the standard error shape)
 *   4. rate-limit per user
 */
export function createApp(deps: AppDeps): RequestListener {
  const now = deps.now ?? (() => new Date());
  const router = new Router();
  registerRoutes(router, { store: deps.store, now });
  const limiter = new RateLimiter(deps.rateLimitPerMinute);

  return (req, res) => {
    void handle(req, res);
  };

  async function handle(req: IncomingMessage, res: ServerResponse): Promise<void> {
    const started = Date.now();
    const requestId = pickRequestId(req);
    const method = (req.method ?? 'GET').toUpperCase();
    let pathname = '/';
    let uidForLog: string | undefined;
    let status = 500;

    try {
      const url = new URL(req.url ?? '/', 'http://localhost');
      pathname = url.pathname;

      // Public liveness check (used by Render's health check). No auth, no data.
      if (pathname === '/health' && method === 'GET') {
        status = send(res, requestId, { body: { status: 'ok', time: now().toISOString() } });
        return;
      }

      const match = router.match(method, pathname);
      if (match.kind === 'not-found') throw new ApiError(404, 'ROUTE_NOT_FOUND', `No route for ${method} ${pathname}`);
      if (match.kind === 'method-not-allowed') {
        res.setHeader('Allow', match.allowed.join(', '));
        throw new ApiError(405, 'METHOD_NOT_ALLOWED', `${method} is not allowed here`);
      }

      // ---- authentication ----
      const token = bearerToken(req);
      const user = token ? await deps.verifier.verify(token) : null;
      if (!user || !isSafeId(user.uid)) throw unauthenticated();
      uidForLog = user.uid;

      // ---- rate limit (per user) ----
      const rl = limiter.hit(user.uid);
      if (!rl.ok) {
        res.setHeader('Retry-After', String(rl.retryAfterSec));
        throw new ApiError(429, 'RATE_LIMITED', 'Too many requests, slow down');
      }

      const body = BODY_METHODS.has(method) ? await readJsonBody(req, deps.maxBodyBytes) : undefined;

      const result = await match.handler({
        uid: user.uid,
        email: user.email,
        params: match.params,
        query: url.searchParams,
        body,
        requestId,
      });
      status = send(res, requestId, result);
    } catch (err) {
      status = sendError(res, requestId, err, deps.logger);
    } finally {
      deps.logger.info('request', {
        requestId,
        method,
        path: pathname,
        status,
        ms: Date.now() - started,
        // Only a short prefix of the user id is logged (privacy).
        user: uidForLog ? uidForLog.slice(0, 6) : undefined,
      });
    }
  }
}

function pickRequestId(req: IncomingMessage): string {
  const h = req.headers['x-request-id'];
  return typeof h === 'string' && /^[A-Za-z0-9_-]{8,64}$/.test(h) ? h : randomUUID();
}

function bearerToken(req: IncomingMessage): string | undefined {
  const h = req.headers.authorization;
  if (typeof h !== 'string') return undefined;
  const m = /^Bearer\s+(\S{1,4096})$/i.exec(h);
  return m?.[1];
}

async function readJsonBody(req: IncomingMessage, maxBytes: number): Promise<unknown> {
  const declared = Number(req.headers['content-length'] ?? 0);
  if (declared > maxBytes) throw new ApiError(413, 'PAYLOAD_TOO_LARGE', `Body must be at most ${maxBytes} bytes`);

  const chunks: Buffer[] = [];
  let size = 0;
  let tooLarge = false;
  await new Promise<void>((resolve, reject) => {
    req.on('data', (chunk: Buffer) => {
      size += chunk.length;
      if (size > maxBytes) tooLarge = true; // keep draining, but stop storing
      else chunks.push(chunk);
    });
    req.on('end', resolve);
    req.on('error', reject);
  });
  if (tooLarge) throw new ApiError(413, 'PAYLOAD_TOO_LARGE', `Body must be at most ${maxBytes} bytes`);
  if (size === 0) return undefined;

  const type = String(req.headers['content-type'] ?? '');
  if (!type.toLowerCase().startsWith('application/json')) {
    throw new ApiError(415, 'UNSUPPORTED_MEDIA_TYPE', 'Content-Type must be application/json');
  }
  try {
    return JSON.parse(Buffer.concat(chunks).toString('utf8'));
  } catch {
    throw new ApiError(400, 'INVALID_JSON', 'Request body is not valid JSON');
  }
}

function baseHeaders(requestId: string): Record<string, string> {
  return {
    'Content-Type': 'application/json; charset=utf-8',
    'Cache-Control': 'no-store',
    'X-Content-Type-Options': 'nosniff',
    'X-Request-Id': requestId,
  };
}

function send(res: ServerResponse, requestId: string, result: Result): number {
  const status = result.status ?? (result.body === undefined ? 204 : 200);
  const headers = { ...baseHeaders(requestId), ...result.headers };
  if (status === 204) {
    res.writeHead(204, headers);
    res.end();
  } else {
    res.writeHead(status, headers);
    res.end(JSON.stringify(result.body));
  }
  return status;
}

function sendError(res: ServerResponse, requestId: string, err: unknown, logger: Logger): number {
  if (res.headersSent) {
    res.end();
    return res.statusCode;
  }
  if (err instanceof ApiError) {
    res.writeHead(err.status, baseHeaders(requestId));
    res.end(JSON.stringify({ error: { code: err.code, message: err.message, details: err.details, requestId } }));
    return err.status;
  }
  // Unexpected failure: log the detail on the server, tell the client nothing internal.
  logger.error('unhandled error', { requestId, error: err instanceof Error ? (err.stack ?? err.message) : String(err) });
  res.writeHead(500, baseHeaders(requestId));
  res.end(JSON.stringify({ error: { code: 'INTERNAL', message: 'Something went wrong', requestId } }));
  return 500;
}
