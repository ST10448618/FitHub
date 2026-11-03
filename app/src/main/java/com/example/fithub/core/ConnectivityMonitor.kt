package com.example.fithub.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for whether the device currently has a *usable*
 * internet connection.
 *
 * "Usable" means the active network advertises both
 * [NetworkCapabilities.NET_CAPABILITY_INTERNET] **and**
 * [NetworkCapabilities.NET_CAPABILITY_VALIDATED] — i.e. captive-portal or
 * dead-airplane-with-Wi-Fi cases report as **offline**, which is what the
 * offline-sync UX needs.
 *
 * Reference: https://developer.android.com/training/monitoring-device-state/connectivity-status-type
 *            https://developer.android.com/reference/android/net/ConnectivityManager#registerDefaultNetworkCallback(android.net.ConnectivityManager.NetworkCallback)
 * (accessed February 2026)
 */
class ConnectivityMonitor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val cm: ConnectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(readInitialState())
    /** `true` only when there is a validated internet-capable network. */
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback: ConnectivityManager.NetworkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // onAvailable does NOT guarantee internet. Recompute from active caps.
            publish(readCurrentCapabilities(), reason = "onAvailable")
        }
        override fun onLost(network: Network) {
            publish(readCurrentCapabilities(), reason = "onLost")
        }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            publish(evaluate(caps), reason = "onCapabilitiesChanged")
        }
    }

    init {
        // Reference: https://developer.android.com/develop/connectivity/network-ops/reading-network-state
        try {
            cm.registerDefaultNetworkCallback(callback)
            Log.i(LogTags.NET, "ConnectivityMonitor registered; initial online=${_isOnline.value}")
        } catch (t: Throwable) {
            // Never crash the app because the OS rejected the callback.
            Log.e(LogTags.NET, "Failed to register network callback", t)
        }
    }

    private fun publish(newValue: Boolean, reason: String) {
        val old = _isOnline.value
        _isOnline.value = newValue
        if (old != newValue) {
            Log.i(LogTags.NET, "Connectivity changed ($reason): online=$newValue")
        } else {
            Log.d(LogTags.NET, "Connectivity unchanged ($reason): online=$newValue")
        }
    }

    private fun readInitialState(): Boolean = readCurrentCapabilities()

    private fun readCurrentCapabilities(): Boolean {
        val active = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(active) ?: return false
        return evaluate(caps)
    }

    private fun evaluate(caps: NetworkCapabilities): Boolean =
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}