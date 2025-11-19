package com.toh.shared

import androidx.annotation.VisibleForTesting

/** Define action paths for communication between wearable and connected phone */
object WearableActionPath {
    /** Notify that the unit settings have changed on the connected phone */
    const val SYNC_APP_SETTING = "/sync-app-setting"

    /** Check if the companion app is installed on the connected phone */
    const val CHECK_APP_INSTALLED = "/check-app-installed"

    /** Reply from the connected phone that the companion app is installed */
    const val PING_PHONE = "/ping-phone"

    /**  Request to sync the audio list from the connected phone */
    const val SYNC_LIST_AUDIO = "/sync-list-audio"

    /** Request to download a file from the connected phone */
    const val REQUEST_DOWNLOAD_FILE = "/request-download-file"
}