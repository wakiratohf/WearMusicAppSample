package com.toh.phone.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    fun copyToTempFile(context: Context, uri: Uri): File {
        val fileName = "temp_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)

        context.contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(tempFile).use { output ->
                input?.copyTo(output)
            }
        }
        return tempFile
    }
}