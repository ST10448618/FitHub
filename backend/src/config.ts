// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
/**
 * Runtime configuration, read once from environment variables.
 * Secrets (the Firebase service account) are never committed; they come from
 * the host's environment (Render dashboard) or a local .env file that is
 * git-ignored.
 */
export interface Config {
  port: number;
  nodeEnv: string;
  rateLimitPerMinute: number;
  maxBodyBytes: number;
  /** Base64 of the service-account JSON. When absent, Application Default Credentials are used. */
  serviceAccountB64: string | undefined;
}

function intFromEnv(value: string | undefined, fallback: number, name: string): number {
  if (value === undefined || value === '') return fallback;
  const n = Number(value);
  if (!Number.isInteger(n) || n <= 0) throw new Error(`Environment variable ${name} must be a positive integer`);
  return n;
}

export function loadConfig(env: NodeJS.ProcessEnv = process.env): Config {
  return {
    port: intFromEnv(env.PORT, 8080, 'PORT'),
    nodeEnv: env.NODE_ENV ?? 'development',
    rateLimitPerMinute: intFromEnv(env.RATE_LIMIT_PER_MINUTE, 120, 'RATE_LIMIT_PER_MINUTE'),
    maxBodyBytes: intFromEnv(env.MAX_BODY_BYTES, 100 * 1024, 'MAX_BODY_BYTES'),
    serviceAccountB64: env.FIREBASE_SERVICE_ACCOUNT_B64 || undefined,
  };
}