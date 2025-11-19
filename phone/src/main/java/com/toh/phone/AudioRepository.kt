package com.toh.phone

import android.content.Context
import android.provider.MediaStore
import com.toh.shared.model.AudioItem

object AudioRepository {
    fun getAudioList(context: Context): List<AudioItem> {
        val audioList = mutableListOf<AudioItem>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI


        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE
        )


        val cursor = context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )


        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)


            while (it.moveToNext()) {
                audioList.add(
                    AudioItem(
                        id = it.getLong(idCol),
                        title = it.getString(titleCol) ?: "",
                        fileName = it.getString(nameCol) ?: "",
                        duration = it.getLong(durationCol),
                        size = it.getLong(sizeCol)
                    )
                )
            }
        }


        return audioList
    }
}