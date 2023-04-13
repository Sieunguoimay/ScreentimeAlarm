package com.sieunguoimay.screentimealarm.data

import android.os.Build
import android.util.Log
import java.lang.Long.max
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class ViewData(
    val alarmData: AlarmData
) {
    val remainingSeconds: Long
        get() = (remainingMilliSeconds) / 1000L
    val remainingMilliSeconds: Long
        get() = max(alarmData.alarmRuntimeData.alarmFireTime - System.currentTimeMillis(), 0L)
    val remainingTimeFormatted: String
        get() = formatTime(remainingMilliSeconds)
    val progressedTimeFormatted: String
        get() = formatTime(alarmData.alarmConfigData.maxScreenTimeMilliSeconds - remainingMilliSeconds)

    companion object {
        fun formatTime(milliSeconds: Long): String {
            val hours = TimeUnit.MILLISECONDS.toHours(milliSeconds)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliSeconds) % 60

            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }
}

class AlarmData(
    val alarmConfigData: AlarmConfigData,
    val alarmRuntimeData: AlarmRuntimeData
)