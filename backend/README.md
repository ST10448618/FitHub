# FitHub REST API (PROG7314 Phase 6B)

Server-side companion to the FitHub Android app. Stateless Node 22 service
that authenticates Firebase ID tokens, aggregates Firestore data, and
exposes a small REST surface under `/api/v1/...`.

## Stack

- Node 22.18+, ESM, TypeScript (`.ts` extensions on every relative import)
- `firebase-admin` ^13 (only runtime dependency)
- `node:test` for tests
- Hosted on Render.com free tier (`render.yaml` at repo root)

## Layout

```
src/
  app.ts                       HTTP request pipeline: auth → rate-limit → route → JSON
  server.ts                    Entry point (createServer + listen)
  config.ts                    Env loading (PORT, Firebase credentials)
  auth/
    tokenVerifier.ts           TokenVerifier interface + VerifiedUser (carries claims)
  firebase/
    admin.ts                   firebase-admin bootstrap
    firebaseTokenVerifier.ts   Real ID-token verification
    firestoreStore.ts          Firestore-backed Store
  http/
    router.ts                  Tiny Router (METHOD + path, :param segments)
    errors.ts                  ApiError + response helpers
    validate.ts                Validator (str/num/email/oneOf/date/timestamp/…)
    logger.ts                  Structured logger
    rateLimit.ts               In-memory token bucket (per uid)
  store/
    store.ts                   Store interface + Query + Doc
    memoryStore.ts             In-memory (tests)
    firestoreStore.ts          Firestore (prod)
    paths.ts                   Doc path helpers (`paths.user(uid)`, …)
    lww.ts                     resolveLww — last-write-wins
  domain/
    types.ts                   Shared enums / DTO interfaces
    calculators.ts             Mifflin-St Jeor, macros, meal split, goal direction
    streakCalculator.ts        Pure-TS port of the Android StreakCalculator
  routes/
    index.ts                   registerRoutes — the one place new route modules are added
    deps.ts                    RouteDeps + typed Doc readers (readStr/readNum/readBool)
    profile.ts                 GET/PUT /me, POST /me/onboarding  (Phase 6A)
    summary.ts                 GET /me/summary                   (Phase 6B)
    fcmToken.ts                POST /me/fcm-token                (Phase 6B)
    feedback.ts                POST /me/feedback                 (Phase 6B)
    broadcast.ts               POST /admin/broadcast             (Phase 6B, admin claim)
test/
  helpers.ts                   startTestApi, FakeVerifier, profileBody
  calculators.test.ts          BMR / macros / direction / meal split
  http.test.ts                 Router basics + HTTP errors
  profile.test.ts              Profile CRUD + onboarding
  store.test.ts                LWW + MemoryStore
  validate.test.ts             Validator
  phase6b.test.ts              Phase 6B endpoints
```

## Endpoints

| Method | Path | Auth | Notes |
|--------|------|------|-------|
| GET | `/health` | none | Render health check |
| GET | `/api/v1/me` | user | profile |
| PUT | `/api/v1/me` | user | LWW upsert |
| POST | `/api/v1/me/onboarding` | user | completes onboarding (idempotent) |
| GET | `/api/v1/me/summary?days=N` | user | calories / weight / workouts / streak, N ∈ [1,90] |
| POST | `/api/v1/me/fcm-token` | user | `{ token, language? }` |
| POST | `/api/v1/me/feedback` | user | `{ rating:1..5, message?, appVersion? }` |
| POST | `/api/v1/admin/broadcast` | admin claim | `{ title, body, topic }` |

## Local development

```powershell
cd backend
npm install
npm run typecheck     # tsc --noEmit
npm test              # node --test test/*.test.ts
npm run build         # tsc -p tsconfig.build.json → dist/
npm run dev           # node --watch src/server.ts
```

Server listens on `PORT` (default 8080). Without `FIREBASE_SERVICE_ACCOUNT_B64`
set, `firebase-admin` falls back to Application Default Credentials. For unit
tests, `MemoryStore` + `FakeVerifier` are used — no Firebase project needed.

## Deploy to Render

1. Push the repo. `render.yaml` at the **repo root** registers the service.
2. Render Dashboard → New → Blueprint → pick the repo → it reads `render.yaml`.
3. Set `FIREBASE_SERVICE_ACCOUNT_B64` in the Render dashboard (base64 of your
   `serviceAccountKey.json` — never commit it).
4. Wait for `npm ci && npm run build` → `node dist/server.js` → `/health` green.

Free tier spins down after 15 min idle; first request after wake ~30 s.

## Admin claim

See `scripts/set-admin-claim.md`. Required to call `/api/v1/admin/broadcast`.

## Tests

- **Phase 6A** — `calculators.test.ts`, `http.test.ts`, `profile.test.ts`, `store.test.ts`, `validate.test.ts`
- **Phase 6B** — `phase6b.test.ts` (summary, fcm-token, feedback, broadcast-403)

Run all: `npm test` (uses `node --test test/*.test.ts`).