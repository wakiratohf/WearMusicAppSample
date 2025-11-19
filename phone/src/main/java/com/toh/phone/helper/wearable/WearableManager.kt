package com.toh.wearossample.helper.wearable

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.wearable.Wearable
import com.toh.phone.ApplicationModules
import kotlin.let

@SuppressLint("LogNotTimber")
class WearableManager(applicationContext: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private val appContext: Context = applicationContext.applicationContext
    private var googleApiClient: GoogleApiClient? = null
    private var _isWearApiAvailable: Boolean = false
    val isWearApiAvailable: Boolean
        get() = _isWearApiAvailable

    companion object {
        private const val TAG = "WearableManager"
    }

    private fun hasWatchFeature(): Boolean {
        return appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_WATCH)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(appContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.w(TAG, "Google Play Services is not available or outdated. Error code: $resultCode. User can fix.")
                // Trong một Activity, có thể gọi như sau để user tự fix lỗi:
                // apiAvailability.getErrorDialog(activity, resultCode, YOUR_REQUEST_CODE_FOR_PLAY_SERVICES)?.show()
            } else {
                Log.e(TAG, "This device does not support Google Play Services. Error code: $resultCode")
            }
            return false
        }
        return true
    }

    fun connect() {
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Cannot connect to Wearable API: Google Play Services not available.")
            _isWearApiAvailable = false
            return
        }

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(appContext)
                .addApi(Wearable.API) // Vẫn thêm API bạn muốn sử dụng
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        }

        googleApiClient?.let { client ->
            if (!client.isConnected && !client.isConnecting) {
                Log.d(TAG, "Attempting to connect to GoogleApiClient for Wearable.API...")
                client.connect()
            } else if (client.isConnected) {
                Log.d(TAG, "GoogleApiClient already connected.")
                // Gọi onConnected để đảm bảo logic sử dụng API được kích hoạt nếu client đã kết nối sẵn
                onConnected(null)
            }
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.i(TAG, "GoogleApiClient connected successfully to Wearable.API.")
        _isWearApiAvailable = true
        // Init wearable khi device hỗ trợ Wearable.API
        ApplicationModules.instant.initWearable()
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.w(TAG, "GoogleApiClient connection suspended. Cause: $cause")
        _isWearApiAvailable = false // API không còn khả dụng ngay lập tức
        // Bạn có thể thử kết nối lại khi thích hợp, ví dụ: googleApiClient?.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.e(TAG, "GoogleApiClient connection failed. ErrorCode: ${result.errorCode}, Message: ${result.errorMessage}")
        _isWearApiAvailable = false

        when (result.errorCode) {
            ConnectionResult.API_UNAVAILABLE -> {
                Log.w(TAG, "Wearable.API is not available on this device. " +
                            "This might be a non-wear device, or Play Services is missing/outdated for Wear support."
                )
                // Xử lý cụ thể cho API_UNAVAILABLE:
                // - Không thử kết nối lại cho API này nữa trong phiên hiện tại (hoặc có chiến lược backoff rất dài).
                // - Vô hiệu hóa các tính năng của ứng dụng phụ thuộc vào Wearable.API.
            }
        }
    }

    fun disconnect() {
        googleApiClient?.let { client ->
            if (client.isConnected || client.isConnecting) {
                Log.d(TAG, "Disconnecting GoogleApiClient.")
                client.disconnect()
            }
        }
        _isWearApiAvailable = false
        googleApiClient = null // Để có thể tạo lại nếu cần
    }
}
