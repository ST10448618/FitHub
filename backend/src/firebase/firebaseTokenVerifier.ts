import { getAuth } from 'firebase-admin/auth';
import type { App } from 'firebase-admin/app';
import type { TokenVerifier, VerifiedUser } from '../auth/tokenVerifier.ts';

/**
 * Real token verification with the Firebase Admin SDK.
 * verifyIdToken checks the signature, issuer, audience (our project) and expiry.
 * Reference: https://firebase.google.com/docs/auth/admin/verify-id-tokens
 */
export class FirebaseTokenVerifier implements TokenVerifier {
  private readonly app: App;

  constructor(app: App) {
    this.app = app;
  }

  async verify(token: string): Promise<VerifiedUser | null> {
    try {
      const decoded = await getAuth(this.app).verifyIdToken(token);
      return { uid: decoded.uid, email: decoded.email };
    } catch {
      return null; // expired, forged or malformed: the caller answers 401
    }
  }
}
