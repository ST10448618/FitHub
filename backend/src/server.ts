// AI-assisted (DeepSeek); reviewed and tested by the FitHub team.
import { createServer } from 'node:http';
import { createApp } from './app.ts';
import { loadConfig } from './config.ts';
import { consoleLogger } from './http/logger.ts';
import { getFirebaseApp } from './firebase/admin.ts';
import { FirebaseTokenVerifier } from './firebase/firebaseTokenVerifier.ts';
import { FirestoreStore } from './firebase/firestoreStore.ts';

/**
 * Entry point: wires the real Firebase pieces into the app and starts listening.
 * Reference: https://firebase.google.com/docs/admin/setup
 * (accessed February 2026)
 */
const config = loadConfig();
const firebase = getFirebaseApp(config.serviceAccountB64);

const app = createApp({
  verifier: new FirebaseTokenVerifier(firebase),
  store: new FirestoreStore(firebase),
  logger: consoleLogger,
  rateLimitPerMinute: config.rateLimitPerMinute,
  maxBodyBytes: config.maxBodyBytes,
});

const server = createServer(app);
// Render closes idle connections after ~60 s; keep ours slightly longer.
server.keepAliveTimeout = 65_000;
server.headersTimeout = 66_000;

server.listen(config.port, () => {
  consoleLogger.info('FitHub API listening', { port: config.port, env: config.nodeEnv });
});

// Render sends SIGTERM on deploy; finish in-flight requests before exiting.
process.on('SIGTERM', () => {
  consoleLogger.info('SIGTERM received, shutting down');
  server.close(() => process.exit(0));
});