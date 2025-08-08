package com.file.easyfilerecovery.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CommonUtils {

    fun getPastTimeRange(monthsAgo: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        return calendar.timeInMillis
    }

    fun formatDateTime(time: Long, pattern: String): String {
        return runCatching { SimpleDateFormat(pattern, Locale.getDefault()).format(time) }.getOrNull() ?: ""
    }

}