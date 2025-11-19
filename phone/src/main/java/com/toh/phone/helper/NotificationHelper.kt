package com.toh.phone.helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.toh.phone.R

object NotificationHelper {
    @SuppressLint("MissingPermission")
    fun showNotification(
        context: Context,
        bridgeTag: String? = null,
        channelId: String = "default_channel",
        channelName: String = "Default",
        notificationId: Int = 1234,
        title: String = "Sample Notification",
        text: String = "This is a sample notification from Wear OS app.",
        smallIcon: Int = R.drawable.ic_launcher_foreground
    ) {
        val wearablePage = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Details")
            .setContentText("Timestamp: ${System.currentTimeMillis()}")
            .setSmallIcon(smallIcon)
            .build()
        val wearableExtender = NotificationCompat.WearableExtender()
            .apply {
                bridgeTag?.let { setBridgeTag(it) }
                setDismissalId("sample_dismissal_id_${System.currentTimeMillis()}")
            }
            .addPage(wearablePage)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
                nm.createNotificationChannel(channel)
            }
        }
        val launchIntentForPackage = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title + if (bridgeTag != null) " (Bridge: $bridgeTag)" else "")
            .setContentText(text + " " + System.currentTimeMillis())
            .setContentIntent(PendingIntent.getActivity(context, 0, launchIntentForPackage, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .setSmallIcon(smallIcon)

            .setAutoCancel(true)
            .setLocalOnly(false)
            .extend(wearableExtender)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}