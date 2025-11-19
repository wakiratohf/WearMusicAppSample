package com.toh.wearmusicapp.helper.networkstate

interface NetworkStateListener {
    fun onNetworkAvailable()
    fun onNetworkLost() {}
}
