# Boubou Android App - Firebase Setup

This document provides detailed instructions on setting up Firebase for the Boubou Android application, including Authentication (Email/Password and optional Google Sign-In) and Firestore.

---

## 1. Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Click **Create a new Firebase project**.
3. Enter a project name (e.g., `Boubou`), accept the terms and create the project.

---

## 2. Register Android App in Firebase

1. Go to **Project Settings → General  → Your apps** and choose the Android icon.
2. Enter the **package name** `com.vasrask.boubou` (or your own username) and optionally enter an App nickname.
3. Click **Register app**.
4. Downlaod `google-services.json` into your module (app-level) root directory (can be done later).
5. Add Firebase SDK (Choose `Kotlin DSL`).
6. You're all set!

---

## 3. Add SHA-1 and SHA-256 Fingerprints

1. Run the following in your android project root:

```bash
./gradlew signingReport
```

2. Locate SHA-1 and SHA-256 for debug and/or release build variants.
3. Go to **Firebase Console → Project Settings → Your Android app → Add fingerprint**.
4. Add both SHA-1 and SHA-256 and click **Save**.

> SHA fingerprints are required for Google Sign-In and Recaptcha verification in Firebase Auth.

---

## 4. Download `google-services.json`

1. After adding SHA fingerprints, download `google-services.json` into your module (app-level) root directory .
2. Sync project in Android Studio.

---


## 5. Enable Authentication Methods

1. Go to **Firebase Console → Build → Authentication → Get started → Sign-in method**.
2. Enable **Email/Password** and optionally **Google Sign-In**.

> Ensure SHA fingerprints are correct if using Google Sign-In.

---

## 6. Set Firestore Rules
1. Go to **Build → Firestore Database → Create database**  in either production or test mode.
2. Go to **Firestore Database → Rules** and set:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /babyActivities/{sessionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}

```
3. Click **Publish**.
> Ensures users can only access their own data.

---
## 7. Verify Your Setup

Once you’ve completed all the steps above:
1. Sync your Gradle project in Android Studio
2. Build and run the app on your device or emulator.
3. Open **Logcat** and confirm that Firebase is connected:
   ```
   FirebaseApp initialization successful
   ```  
4. Try registering or logging in to ensure Authentication and Firestore are working as expected.

---

Your Firebase setup for **Boubou** is now complete! 
If something doesn’t work as expected:
- Double-check your `google-services.json` (ensure it is in `app/` and up-to-date).  
- Ensure your **SHA-1** and **SHA-256** fingerprints are added correctly in Firebase.  
- Re-sync and rebuild the project.

