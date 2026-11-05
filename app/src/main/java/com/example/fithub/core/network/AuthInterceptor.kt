package com.example.fithub.core.network

import android.util.Log
import com.example.fithub.core.LogTags
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Phase 6B: attaches `Authorization: Bearer <Firebase ID token>` to every
 * outgoing REST call.
 *
 * ### Why `runBlocking`
 * OkHttp runs interceptors on its own dispatcher thread, not on the caller's
 * coroutine context. Firebase's `getIdToken()` returns a `Task`, which we
 * await on a blocking call. This is the standard pattern — see
 * https://firebase.google.com/docs/auth/admin/verify-id-tokens (accessed Feb 2026).
 *
 * ### Why `getIdToken(false)` not `getIdToken(true)`
 * `false` uses the cached token if it hasn't expired (>5 min remaining) and
 * only refreshes when needed. `true` would force a network refresh on every
 * call — unnecessary and slow.
 *
 * ### On failure
 * If no user is signed in, or the token fetch fails, we send the request
 * without an Authorization header. The server responds 401, which the repository
 * surfaces as a typed error. We do NOT throw from the interceptor, because that
 * would abort unrelated calls.
 */
class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            Log.w(LogTags.NETWORK, "AuthInterceptor: no signed-in user, sending request without token")
            return chain.proceed(original)
        }

        val token = try {
            runBlocking { user.getIdToken(false).await()?.token }
        } catch (t: Throwable) {
            Log.e(LogTags.NETWORK, "AuthInterceptor: token fetch failed", t)
            null
        }

        if (token.isNullOrBlank()) {
            Log.w(LogTags.NETWORK, "AuthInterceptor: empty token, sending request without Authorization")
            return chain.proceed(original)
        }

        val authorized = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authorized)
    }
}