// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { getAuth } from 'firebase-admin/auth';
import type { App } from 'firebase-admin/app';
import type { TokenVerifier, VerifiedUser } from '../auth/tokenVerifier.ts';

/**
 * Real token verification with the Firebase Admin SDK.
 * verifyIdToken checks the signature, issuer, audience (our project) and expiry.
 *
 * Reference: https://firebase.google.com/docs/auth/admin/verify-id-tokens
 * (accessed February 2026)
 */
export class FirebaseTokenVerifier implements TokenVerifier {
  private readonly app: App;

  constructor(app: App) {
    this.app = app;
  }

  async verify(token: string): Promise<VerifiedUser | null> {
    try {
      const decoded = await getAuth(this.app).verifyIdToken(token);
      // `decoded` includes the standard fields plus any custom claims set on
      // the Firebase Auth user (e.g. `admin: true`). We strip the standard
      // fields so `claims` stays small and predictable.
      const {
        uid,
        email,
        aud,
        auth_time,
        exp,
        firebase,
        iat,
        iss,
        sub,
        ...customClaims
      } = decoded as unknown as Record<string, unknown> & {
        uid: string;
        email?: string;
      };
      void aud; void auth_time; void exp; void firebase; void iat; void iss; void sub;
      return { uid, email, claims: customClaims };
    } catch {
      return null; // expired, forged or malformed: the caller answers 401
    }
  }
}