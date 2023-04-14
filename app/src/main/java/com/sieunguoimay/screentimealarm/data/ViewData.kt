package com.sieunguoimay.screentimealarm.data

import java.lang.Long.max
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ViewData(
    val alarmData: AlarmData
) {
    val remainingSeconds: Long
        get() = (remainingMilliSeconds) / 1000L
    val remainingMilliSeconds: Long
        get() = max(alarmData.alarmRuntimeData.alarmFireTime - System.currentTimeMillis(), 0L)

    fun getRemainingTimeFormatted(): String {
        return formatTime(remainingMilliSeconds)

    }

    fun getProgressedTimeFormatted(): String {
        val milliSeconds =
            alarmData.alarmConfigData.maxScreenTimeMilliSeconds - remainingMilliSeconds
        return formatTime(milliSeconds)
    }

    companion object {
        fun formatTime(milliseconds: Long): String {

            val unitNames = arrayOf(
                "d", "h", "m", "s"
            )

            val formattedTime = StringBuilder()
            val days = TimeUnit.MILLISECONDS.toDays(milliseconds)
            val hours = TimeUnit.MILLISECONDS.toHours(milliseconds) % 24
            val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
            val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

            if (days > 0) {
                formattedTime.append("$days${unitNames[0]}")
            }
            if (hours > 0) {
                formattedTime.append("$hours${unitNames[1]}")
            }
            if (minutes > 0) {
                formattedTime.append("$minutes${unitNames[2]}")
            }
            formattedTime.append("$seconds${unitNames[3]}")
            return formattedTime.toString()
        }
    }
}

class AlarmData(
    val alarmConfigData: AlarmConfigData,
    val alarmRuntimeData: AlarmRuntimeData
)