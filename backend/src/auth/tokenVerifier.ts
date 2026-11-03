/**
 * Verifies the Firebase ID token sent by the app. Kept behind an interface so the real
 * Firebase Admin implementation (src/firebase/) can be swapped for a fake in tests.
 */
export interface VerifiedUser {
  uid: string;
  email: string | undefined;
}

export interface TokenVerifier {
  /** Returns the user when the token is valid and unexpired, otherwise null. Never throws for bad tokens. */
  verify(token: string): Promise<VerifiedUser | null>;
}
