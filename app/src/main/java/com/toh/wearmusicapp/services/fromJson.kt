package com.toh.wearmusicapp.services

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.utility.DebugLog
import org.json.JSONObject

inline fun <reified T : Any> String?.fromJson(): T? = this?.let {
    val type = object : TypeToken<T>() {}.type
    Gson().fromJson(this, type)
}

inline fun <reified T : Any> T?.json() = this?.let { Gson().toJson(this, T::class.java) }

fun String?.parseJsonConfigToIntHashMap(): LinkedHashMap<String, Int> {
    if (this.isNullOrEmpty()) return LinkedHashMap()
    try {
        // Parse config
        val states = LinkedHashMap<String, Int>()
        val jsonObject = JSONObject(this)
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            states[key] = jsonObject.getInt(key)
        }
        return states
    } catch (e: java.lang.Exception) {
        DebugLog.loge(e)
    }
    return LinkedHashMap()
}