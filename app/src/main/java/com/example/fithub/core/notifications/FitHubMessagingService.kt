
package com.example.fithub.core.notifications

import android.util.Log
import com.example.fithub.R
import com.example.fithub.core.LogTags
import com.example.fithub.core.SessionManager
import com.example.fithub.core.ServiceLocator
import com.example.fithub.data.remote.FirestorePaths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FitHubMessagingService : FirebaseMessagingService() {

    private val serviceJob = SupervisorJob()

    private val serviceScope =
        CoroutineScope(serviceJob + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.i(
            LogTags.PUSH,
            "onNewToken received token=${token.take(8)}…"
        )

        serviceScope.launch {

            // Save token locally.
            runCatching {
                ServiceLocator.preferencesManager.setFcmToken(token)
            }.onFailure {
                Log.e(
                    LogTags.PUSH,
                    "Saving FCM token locally failed",
                    it
                )
            }

            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: SessionManager.currentUserId

            if (uid == null) {
                Log.d(
                    LogTags.PUSH,
                    "No active session — token saved locally only"
                )
                return@launch
            }

            // Mirror token to Firestore.
            runCatching {
                val language = languageCodeForUser()

                FirestorePaths.user(uid).set(
                    mapOf(
                        "fcmToken" to token,
                        "fcmTokenUpdatedAt" to FieldValue.serverTimestamp(),
                        "language" to language
                    ),
                    SetOptions.merge()
                )

                Log.i(
                    LogTags.PUSH,
                    "FCM token mirrored to users/${uid.take(8)}…"
                )
            }.onFailure {
                Log.e(
                    LogTags.PUSH,
                    "Firestore token mirror failed",
                    it
                )
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.i(
            LogTags.PUSH,
            "FCM message received " +
                    "hasNotification=${remoteMessage.notification != null} " +
                    "dataKeys=${remoteMessage.data.keys}"
        )

        serviceScope.launch {

            val enabled = runCatching {
                ServiceLocator.preferencesManager
                    .pushNotificationsEnabled
                    .first()
            }.getOrDefault(true)

            if (!enabled) {
                Log.i(
                    LogTags.PUSH,
                    "Notification ignored — push disabled"
                )
                return@launch
            }

            // Avoid posting duplicate notifications.
            if (remoteMessage.notification != null) {
                Log.d(
                    LogTags.PUSH,
                    "Notification payload already handled by FCM"
                )
                return@launch
            }

            val title = remoteMessage.data["title"]
                ?: getString(R.string.notif_fallback_title)

            val body = remoteMessage.data["body"]
                ?: getString(R.string.notif_fallback_body)

            val route = remoteMessage.data["route"]
            val type = remoteMessage.data["type"]

            if (type != null) {
                Log.d(
                    LogTags.PUSH,
                    "Notification type=$type"
                )
            }

            NotificationHelper.showNotification(
                context = applicationContext,
                notificationId =
                    (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                title = title,
                body = body,
                route = route
            )
        }
    }

    private suspend fun languageCodeForUser(): String {
        return runCatching {
            ServiceLocator.preferencesManager.language
                .first()
                .lowercase()
                .let { language ->
                    if (language.startsWith("english")) {
                        "en"
                    } else {
                        language
                    }
                }
        }.getOrDefault("en")
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
