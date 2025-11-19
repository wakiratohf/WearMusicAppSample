package com.toh.wearmusicapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.concurrent.futures.await
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.toh.shared.WearableActionPath
import com.utility.DebugLog
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

object WearableUtils {
    /**
     * Returns a list of nodes that are paired with the phone.
     * @param context The context to use.
     * @return A list of nodes that are paired with the phone.
     */
    suspend fun getPhonePairedList(context: Context): List<Node> {
        val connectedNodes = Wearable.getNodeClient(context).connectedNodes.await()
        return if (isEmulatorDevice()) {
            connectedNodes
        } else {
            connectedNodes.filter { it.isNearby }
        }
    }

    /**
     * Checks if the device is an emulator.
     * @return True if the device is an emulator, false otherwise.
     */
    fun isEmulatorDevice(): Boolean {
        val fingerprint = Build.FINGERPRINT ?: ""
        val model = Build.MODEL ?: ""
        val product = Build.PRODUCT ?: ""
        val brand = Build.BRAND ?: ""
        val manufacturer = Build.MANUFACTURER ?: ""

        val emulatorIndicators = listOf("generic", "unknown", "sdk", "emulator", "google_sdk", "sdk_gwear", "sdk_gphone", "sdk_google", "x86_64", "emu")

        val checkFingerprint = emulatorIndicators.any { fingerprint.contains(it, ignoreCase = true) }
        val checkModel = emulatorIndicators.any { model.contains(it, ignoreCase = true) }
        val checkProduct = emulatorIndicators.any { product.contains(it, ignoreCase = true) }
        val checkBrandManu = brand.contains("generic", ignoreCase = true) && product.contains("generic", ignoreCase = true)
        val checkManufacturer = manufacturer.contains("genymotion", ignoreCase = true)

        return checkFingerprint || checkModel || checkProduct || checkBrandManu || checkManufacturer
    }

    /**
     * Checks if a specific app is installed on the paired phone.
     * @param context The context to use.
     * @param packageName The package name of the app to check.
     * @return True if the app is installed on the phone, false otherwise.
     */
    suspend fun isAppInstalledOnPhone(context: Context, packageName: String, needThrowException: Boolean = false): Boolean {
        val nodes = getPhonePairedList(context)
        if (nodes.isEmpty()) return false
        val messageClient = Wearable.getMessageClient(context)
        val payload = packageName.toByteArray()
        val responsePath = WearableActionPath.CHECK_APP_INSTALLED
        val responseDeferred = CompletableDeferred<Boolean>()

        val listener = MessageClient.OnMessageReceivedListener { event ->
            if (event.path == responsePath) {
                val isInstalled = String(event.data) == "true"
                responseDeferred.complete(isInstalled)
            }
        }

        messageClient.addListener(listener)
        try {
            messageClient.sendMessage(nodes[0].id, WearableActionPath.CHECK_APP_INSTALLED, payload).await()
            return withTimeoutOrNull(5000) { responseDeferred.await() } ?: false
        } catch (e: Exception) {
            if (needThrowException) {
                throw e
            } else {
                DebugLog.loge(e)
            }
        } finally {
            messageClient.removeListener(listener)
        }
        return false
    }

    /**
     * Returns the local device name.
     * @param context The context to use.
     * @return The local device name, or null if it cannot be determined.
     */
    suspend fun getLocalDeviceName(context: Context): String {
        val localDevice = Wearable.getNodeClient(context).localNode.await()
        return localDevice?.displayName ?: "Wear OS Device"
    }

    /**
     * Sends a custom message to the paired phone.
     * @param context The context to use.
     * @param path The message path.
     * @param data The message payload as ByteArray.
     * @return True if the message was sent successfully, false otherwise.
     */
    suspend fun sendCustomMessageToPhone(context: Context, path: String, data: ByteArray? = null): Boolean {
        Log.i("thoinv", "sendCustomMessageToPhone: " + path + ", data size: " + (data?.size ?: 0))
        val nodes = getPhonePairedList(context)
        if (nodes.isEmpty()) return false
        val messageClient = Wearable.getMessageClient(context)
        return try {
            messageClient.sendMessage(nodes[0].id, path, data).await()
            true
        } catch (e: Exception) {
            DebugLog.loge(e)
            false
        }
    }

    private val openAppMutex = Mutex()

    fun isOpenAppJobRunning() = openAppMutex.isLocked

    @SuppressLint("WearRecents")
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun openAppOrPlayStore(context: Context, phonePackage: String, appInstalledOnPhone: Boolean) {
        openAppMutex.lock()
        try {
            val activityHelper = RemoteActivityHelper(context)
            val deepLink = Intent(
                Intent.ACTION_VIEW,
                "sample://open/home".toUri()
            ).addCategory(Intent.CATEGORY_BROWSABLE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                activityHelper.startRemoteActivity(deepLink).await()
                return
            } catch (e: Throwable) {
                DebugLog.loge(e)
                // Try to open the app normally
                val launchIntent = context.packageManager.getLaunchIntentForPackage(phonePackage)
                if (launchIntent != null) {
                    try {
                        activityHelper.startRemoteActivity(launchIntent).await()
                        return
                    } catch (ex: Throwable) {
                        DebugLog.loge(ex)
                    }
                }
            }
            // If the app is not installed, open the Play Store
            if (!appInstalledOnPhone) {
                val market = Intent(
                    Intent.ACTION_VIEW,
                    "market://details?id=$phonePackage".toUri()
                ).addCategory(Intent.CATEGORY_BROWSABLE).setPackage("com.android.vending")
                try {
                    activityHelper.startRemoteActivity(market).await()
                } catch (e: Throwable) {
                    DebugLog.loge(e)
                    try {
                        val web = Intent(
                            Intent.ACTION_VIEW,
                            "https://play.google.com/store/apps/details?id=$phonePackage".toUri()
                        ).addCategory(Intent.CATEGORY_BROWSABLE)
                        activityHelper.startRemoteActivity(web)
                    } catch (_: Exception) {
                    }
                }
            }
        } finally {
            openAppMutex.unlock()
        }
    }
}