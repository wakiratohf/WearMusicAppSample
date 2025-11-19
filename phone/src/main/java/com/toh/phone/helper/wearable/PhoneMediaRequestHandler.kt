package com.toh.phone.helper.wearable

import android.content.Context
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream

internal class PhoneMediaRequestHandler(val context: Context) {
    @OptIn(DelicateCoroutinesApi::class)
    fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_audio") {
            val data = String(messageEvent.data).split("|")
            val audioId = data.getOrNull(0)?.toLongOrNull() ?: return
            val remotePath = data.getOrNull(1) ?: return

            // TODO: map remotePath/audioId -> File thực tế trên phone
            val audioFile = findAudioFile(remotePath) ?: return
            GlobalScope.launch(Dispatchers.IO) {
                val nodeId = messageEvent.sourceNodeId
                val channel = Wearable.getChannelClient(context)
                    .openChannel(nodeId, "/audio_stream/$audioId")
                    .await()

                val outputStream = Wearable.getChannelClient(context)
                    .getOutputStream(channel)
                    .await()

                FileInputStream(audioFile).use { inStream ->
                    outputStream.use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }
        }
    }


    private fun findAudioFile(remotePath: String): File? {
        val dir = File("", "audio")
        val file = File(dir, remotePath)
        return if (file.exists()) file else null
    }
}