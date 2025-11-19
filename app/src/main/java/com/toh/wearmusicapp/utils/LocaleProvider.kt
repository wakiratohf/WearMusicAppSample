package com.toh.wearmusicapp.utils

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale
import kotlin.apply

@Composable
fun LocaleProvider(
    locale: Locale,
    content: @Composable () -> Unit
) {
    // 1. Lấy Context hiện tại
    val context = LocalContext.current
    // 2. Tạo một ContextWrapper với cấu hình Locale mới
    val configuration = LocalConfiguration.current
    val newConfiguration = Configuration(configuration).apply {
        // Thay đổi Locale
        setLocale(locale)
    }
    // Tạo Context đã được wrap với Configuration mới
    val localizedContext = context.createConfigurationContext(newConfiguration)

    // 3. Sử dụng CompositionLocalProvider để ghi đè LocalContext và LocalConfiguration
    // Khi LocalContext bị ghi đè, stringResource() sẽ lấy chuỗi từ Context mới này.
    CompositionLocalProvider(
        LocalConfiguration provides newConfiguration,
        LocalContext provides localizedContext
    ) {
        content()
    }
}