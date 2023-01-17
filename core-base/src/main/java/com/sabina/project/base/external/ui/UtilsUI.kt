package com.sabina.project.base.external.ui

import java.util.*

object UtilsUI {
    fun convertTimestampToDate(timestamp: Long): String {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val prefix = "0"
        val day = if (calendar.get(Calendar.DAY_OF_MONTH) < 10) prefix + calendar.get(Calendar.DAY_OF_MONTH) else calendar.get(Calendar.DAY_OF_MONTH)
        val month = if (calendar.get(Calendar.MONTH) + 1 < 10) prefix + (calendar.get(Calendar.MONTH) + 1) else (calendar.get(Calendar.MONTH) + 1)
        val hour = if (calendar.get(Calendar.HOUR_OF_DAY) < 10) prefix + calendar.get(Calendar.HOUR_OF_DAY) else calendar.get(Calendar.HOUR_OF_DAY)
        val minute = if (calendar.get(Calendar.MINUTE) < 10) prefix + calendar.get(Calendar.MINUTE) else calendar.get(Calendar.MINUTE)
        return "$day.$month.${calendar.get(Calendar.YEAR)}, $hour:$minute"
    }
}