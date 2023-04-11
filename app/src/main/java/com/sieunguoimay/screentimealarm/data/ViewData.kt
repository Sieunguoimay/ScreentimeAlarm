package com.sieunguoimay.screentimealarm.data

import android.os.Build
import java.lang.Long.max
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class ViewData(
    val alarmData: AlarmData
) {
    val remainingSeconds: Long
        get() = (remainingMilliSeconds) / 1000L
    private val remainingMilliSeconds: Long
        get() = max(alarmData.alarmRuntimeData.alarmFireTime - currentTimeCalendar.timeInMillis,0L)
    val remainingTimeFormatted: String
        get() = formatTime(remainingMilliSeconds)

    private val currentTimeCalendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private fun formatTime(milliSeconds: Long): String {
        return formatter.format(Date(milliSeconds))
    }
}

class AlarmData(
    val alarmConfigData: AlarmConfigData,
    val alarmRuntimeData: AlarmRuntimeData
)