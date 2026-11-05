<p align="center">
  <img src="https://readme-typing-svg.herokuapp.com/?color=212C99&size=45&center=true&vCenter=true&width=1000&lines=FITHUB;Your+All-in-One+Fitness+Companion" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-212C99?style=flat-square&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Architecture-MVVM-6237FF?style=flat-square" />
  <img src="https://img.shields.io/badge/Auth-Firebase%20%2B%20Biometrics-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Database-Firestore%20%2B%20Room-374AFF?style=flat-square" />
  <img src="https://img.shields.io/badge/API-Custom%20REST%20%2B%20OpenFoodFacts-212C99?style=flat-square" />
  <img src="https://img.shields.io/badge/Push-Firebase%20Cloud%20Messaging-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white" />
  <img src="https://img.shields.io/badge/Min%20SDK-API%2026-CCD1F8?style=flat-square" />
</p>

---

## 🎥 Final POE Demonstration Video

<p align="center">
  <a href="YOUR_YOUTUBE_LINK_HERE">
    <img src="https://img.shields.io/badge/YouTube-Watch%20Final%20Demo-FF0000?style=for-the-badge&logo=youtube&logoColor=white"/>
  </a>
</p>

> *Click the badge above to watch the full walkthrough of the FitHub Final POE on a physical Android device, demonstrating SSO, Biometrics, Offline Sync, Push Notifications, Multi-language support, and our Custom REST API.*

---

## 🚀 Final POE: What's New?

This repository represents the **Final POE (v2.0.0)** submission. Building upon the foundational prototype, we successfully integrated all required advanced Android capabilities:

- 🔐 **Biometric Authentication:** Secure fingerprint/face unlock implemented via `androidx.biometric`, seamlessly integrated with the app's cold-start session policy.
- 📶 **Offline Mode with Synchronisation:** Built a robust "Outbox" architectural pattern using Room DB and WorkManager. Users can log meals and weights offline; data auto-syncs to Firestore upon reconnection.
- 🔔 **Real-time Push Notifications:** Implemented Firebase Cloud Messaging (FCM) with deep-linking, local checkpoint reminders (WorkManager), and an in-app diagnostics panel.
- 🌍 **Multi-Language Support:** Full localisation infrastructure supporting **English**, **isiZulu (zu)**, and **Afrikaans (af)** using system locales and `stringResource`.
- ☁️ **Custom REST API (Blob Storage & NoSQL):** Replaced prototype mocks with a custom hosted Express/Node.js API on Firebase Cloud Functions. Integrated Firebase Storage for profile and progress photos.
- 🏆 **User-Defined Feature 1 :** "Daily Streak & Weekly Challenge" tracking
- 🏆 **User-Defined Feature 2 :** "Progress Photo Timeline" gallery.

---

## 🛠️ Quick Start

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Koala or newer)
- [JDK 17](https://adoptium.net/temurin/releases/?version=17)
- Android SDK 26+ (target 37)
- A Firebase project with **Auth**, **Firestore**, **Storage**, and **Cloud Messaging** enabled.

### Installation

**1. Clone and open the project**

```bash
git clone https://github.com/ST10448618/Fithub-PROG7314.git
cd Fithub-PROG7314
```

**2. Add your Firebase configuration**

Download `google-services.json` from your Firebase project.

Place it at: `app/google-services.json`

**3. Build and run**

```bash
./gradlew assembleDebug
```

Open the project in Android Studio → run on a physical device running Android 8.0 (API 26) or newer.

*(Note: A physical device is required to test Biometric Authentication, FCM Push Notifications, and Camera Barcode Scanning).*

---

## 🏗️ System Architecture

FitHub follows a **Layered MVVM architecture**. The Compose UI observes state from ViewModels via `StateFlow`. ViewModels coordinate with domain repositories. Repositories handle Room (offline cache), Firestore (cloud NoSQL), Firebase Storage (blobs), and our Custom REST API.

---

## 🔒 Security & Data Architecture

- **Authentication & SSO:** Users can register via Email/Password or Google Sign-In (Credential Manager). Passwords are hashed via Firebase Auth; the app never sees plaintext credentials.
- **Biometric Session Policy:** To allow fingerprint unlock on cold start, the app conditionally retains the Firebase token in `onCreate()` if the biometric lock is enabled, gating access behind the `BiometricPrompt`.
- **Data Isolation:** Firestore Security Rules strictly enforce that `request.auth.uid == userId`. Users can only read/write their own subcollections.
- **Offline-First Integrity:** Food logs and weights are written to Room instantly. A `pending_sync` table queues the Firestore operations, preventing data loss during network drops.

---

## ⚙️ DevSecOps & CI/CD

To ensure codebase stability outside of local environments, this project utilizes **GitHub Actions**.

- **Automated Builds:** `./gradlew assembleDebug` runs on every push and pull request.
- **Automated Unit Tests:** Over 40 JVM unit tests (via `testDebugUnitTest`) cover the `SyncManager`, `StreakCalculator`, `LocaleCodes`, and core Calorie logic.
- **Linting:** Android Lint checks for potential security and UI/UX issues.
- **Branch Protection:** Pull requests require a passing CI run and code review before merging to `main`.

This repository maintains a rigorous commit history (75+ commits) with multiple contributors.

---

## 📱 Play Store Readiness

The application is fully prepared for Google Play Store submission:

- **Release Notes:** See `RELEASE_NOTES.md` for the full changelog.
- **Assets:** High-fidelity screenshots (1080x1920), a 1024x500 feature graphic, and adaptive launcher icons (including Android 13 monochrome layers) are prepared.
- **Privacy Policy:** Compliant with POPIA (South Africa) regarding user  data.

---


<p align="center">
  <strong>PROG7314 · Programming 3D · Final POE · 2026</strong>
</p>
