package com.mushi.app.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

/** Follows Android's default network for the life of the app. */
class AndroidNetworkMonitor(context: Context) : NetworkMonitor(initiallyOnline = hasNetwork(context)) {
    init {
        connectivity(context).registerDefaultNetworkCallback(
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = onNetworkChanged(true)

                override fun onLost(network: Network) = onNetworkChanged(false)
            })
    }

    private companion object {
        fun connectivity(context: Context) =
            context.getSystemService(ConnectivityManager::class.java)

        fun hasNetwork(context: Context): Boolean {
            val manager = connectivity(context)
            return manager.getNetworkCapabilities(manager.activeNetwork)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }
    }
}
