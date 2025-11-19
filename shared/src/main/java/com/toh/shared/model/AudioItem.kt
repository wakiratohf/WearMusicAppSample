package com.toh.shared.model

import android.net.Uri
import com.toh.shared.utils.WearConst.WEAR_SCHEME

data class AudioItem(
    val id: Long,
    val title: String,
    val fileName: String,
    val duration: Long,
    val size: Long,
    val uri: Uri? = null,
    val phoneNodeId: String? = null, // nodeId của phone gửi sang
    val remotePath: String? = null   // path/fileName dùng để request từ phone
) {
    fun buildRequestUri(): Uri {
        // Uri logic: wear://{nodeId}/audio/{id}
        return Uri.Builder()
            .scheme(WEAR_SCHEME)
            .authority(phoneNodeId ?: "")
            .path("/audio/$id")
            .build()
    }
}