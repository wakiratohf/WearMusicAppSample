package com.toh.shared.extension

import android.util.Log
import com.utility.DataUtils

inline fun <reified T> parseJsonOrNull(json: String?): T? {
    if (json.isNullOrEmpty()) return null
    return try {
        DataUtils.parserObject(json, T::class.java)
    } catch (e: Exception) {
        Log.e(
            "PhoneMediaRequestHandler",
            "Failed to parse JSON for type ${T::class.java.simpleName}: ${e.message}"
        )
        null
    }
}
