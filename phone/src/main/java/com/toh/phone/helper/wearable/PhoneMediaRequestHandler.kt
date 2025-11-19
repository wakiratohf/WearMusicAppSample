package com.toh.phone.helper.wearable

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.blankj.utilcode.util.ToastUtils
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.toh.phone.utils.FileUtils
import com.toh.shared.WearableActionPath
import com.toh.shared.WearableChannelPath
import com.toh.shared.extension.parseJsonOrNull
import com.toh.shared.model.AudioItem
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLEncoder

internal class PhoneMediaRequestHandler(val context: Context) {
    @OptIn(DelicateCoroutinesApi::class)
    fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == WearableActionPath.REQUEST_DOWNLOAD_FILE) {
            val data = parseJsonOrNull<AudioItem>(String(messageEvent.data)) ?: return
            val audioId = data.id
            val audioFile = data.uri
            val audioFileName = URLEncoder.encode(data.fileName, Charsets.UTF_8)
            GlobalScope.launch(Dispatchers.IO) {
                val nodeId = messageEvent.sourceNodeId
                val channel = Wearable.getChannelClient(context)
                    .openChannel(nodeId, "${WearableChannelPath.DOWNLOAD_FILE}/$audioId?name=$audioFileName")
                    .await()
                // copy to temp file before send file
                val fileTemp = FileUtils.copyToTempFile(context, audioFile.toUri())
                Wearable.getChannelClient(context).sendFile(channel, Uri.fromFile(fileTemp)).await()
                Wearable.getChannelClient(context).close(channel)
                // delete temp file after send file success
                fileTemp.delete()

                Log.i(TAG, "onMessageReceived: send file success")
                withContext(Dispatchers.Main) {
                    ToastUtils.showShort("Send file $audioFileName success.")
                }
            }
        }
    }


    fun onDataChanged(uri: Uri, dataEvent: DataEvent) {
        // TODO handle data change event for PhoneMediaRequestHandler
    }

    companion object {
        private const val TAG = "PhoneMediaRequestHandler"
    }
}