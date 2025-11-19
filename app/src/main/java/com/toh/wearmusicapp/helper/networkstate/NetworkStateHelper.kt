package com.toh.wearmusicapp.helper.networkstate

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import java.util.concurrent.CopyOnWriteArraySet


class NetworkStateHelper(val context: Context) {

    private var connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isNetworkCallbackRegistered = false
    var hasInternetConnection = false
    private val listeners = CopyOnWriteArraySet<NetworkStateListener>()

    fun addListener(listener: NetworkStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: NetworkStateListener) {
        listeners.remove(listener)
    }

    fun registerNetworkReceiver() {
        if (!isNetworkCallbackRegistered) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    hasInternetConnection = true
                    listeners.forEach { it.onNetworkAvailable() }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    hasInternetConnection = false
                    listeners.forEach { it.onNetworkLost() }
                }
            }
            connectivityManager?.registerDefaultNetworkCallback(networkCallback!!)
            isNetworkCallbackRegistered = true
        }
    }

    fun unregisterNetworkReceiver() {
        if (isNetworkCallbackRegistered && networkCallback != null) {
            try {
                connectivityManager?.unregisterNetworkCallback(networkCallback!!)
            } catch (_: Exception) {
            }
            isNetworkCallbackRegistered = false
            networkCallback = null
        }
    }

    fun destroy() {
        unregisterNetworkReceiver()
        listeners.clear()
    }
}