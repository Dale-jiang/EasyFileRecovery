package com.file.easyfilerecovery.utils

import java.util.Calendar

object CommonUtils {

    fun getPastTimeRange(monthsAgo: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthsAgo)
        return calendar.timeInMillis
    }

}