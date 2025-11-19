package com.toh.wearmusicapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.os.Process
import com.utility.DebugLog
import kotlinx.coroutines.CoroutineExceptionHandler

class BaseWearApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        WearApplicationModules.Companion.instant.initModules(this)
    }

    private fun isMainProcess(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val myProcess = manager.runningAppProcesses.find {
            it.pid == Process.myPid()
        }
        // So sánh tên process hiện tại với tên package ứng dụng
        return myProcess?.processName == applicationContext.packageName
    }

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        DebugLog.loge(throwable)
    }

    override fun onTerminate() {
        super.onTerminate()
        WearApplicationModules.Companion.instant.onDestroy()
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: BaseWearApplication? = null
        fun coroutineExceptionHandler() = instance?.coroutineExceptionHandler ?: CoroutineExceptionHandler { _, throwable ->
            DebugLog.loge(throwable)
        }
    }

}
