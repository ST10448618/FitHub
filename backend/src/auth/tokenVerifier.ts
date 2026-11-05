// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
/**
 * Verifies the Firebase ID token sent by the app. Kept behind an interface so
 * the real Firebase Admin implementation (src/firebase/) can be swapped for a
 * fake in tests.
 *
 * `claims` carries the custom claims set on the Firebase Auth user (e.g.
 * `admin: true`). Routes that need them read `ctx.claims` — never the raw token.
 *
 * Reference: https://firebase.google.com/docs/auth/admin/custom-claims
 * (accessed February 2026)
 */
export interface VerifiedUser {
  uid: string;
  email: string | undefined;
  /** Custom claims from the ID token, e.g. { admin: true }. Empty object when none. */
  claims: Record<string, unknown>;
}

export interface TokenVerifier {
  /** Returns the user when the token is valid and unexpired, otherwise null. Never throws for bad tokens. */
  verify(token: string): Promise<VerifiedUser | null>;
}