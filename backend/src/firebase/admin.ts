import { applicationDefault, cert, getApps, initializeApp } from 'firebase-admin/app';
import type { App } from 'firebase-admin/app';

/**
 * Initialises the Firebase Admin SDK once.
 * Credentials come from FIREBASE_SERVICE_ACCOUNT_B64 (base64 of the service-account JSON, set in the
 * Render dashboard), or from Application Default Credentials for local use
 * (GOOGLE_APPLICATION_CREDENTIALS pointing at a key file that is NOT committed).
 * Reference: https://firebase.google.com/docs/admin/setup
 */
export function getFirebaseApp(serviceAccountB64: string | undefined): App {
  const existing = getApps()[0];
  if (existing) return existing;
  if (serviceAccountB64) {
    const json = JSON.parse(Buffer.from(serviceAccountB64, 'base64').toString('utf8'));
    return initializeApp({ credential: cert(json) });
  }
  return initializeApp({ credential: applicationDefault() });
}
