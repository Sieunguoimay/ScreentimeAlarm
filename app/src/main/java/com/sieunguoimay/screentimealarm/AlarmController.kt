package com.sieunguoimay.screentimealarm

import android.util.Log
import com.sieunguoimay.screentimealarm.data.AlarmData
import java.util.*

class AlarmController {

    var alarmData: AlarmData? = null
        private set

    private var isRunning: Boolean = false

    val alarmFireHandlers: ArrayList<AlarmFireHandler> = ArrayList()
    val alarmStartOverHandlers: ArrayList<AlarmStartOverHandler> = ArrayList()

    private val minutes: Int get() = alarmData?.alarmConfigData?.maxScreenTimeMinutes ?: 0

    private var timer = Timer()
    fun setAlarmData(alarmData: AlarmData?) {
        this.alarmData = alarmData
    }

    fun startAlarm() {
        if (alarmData == null) {
            Log.e("", "startAlarm $minutes - alarmData is empty")
            return
        }
        if (isRunning) {
            Log.e("", "startAlarm $minutes - alarm already running")
            return
        }
        Log.d("", "startAlarm $minutes")
        isRunning = true
        updateAlarmFireTime()
    }

    fun stopAlarm() {
        isRunning = false
        timer.cancel()
    }

    fun startOver() {
        isRunning = true
        timer.cancel()
        updateAlarmFireTime()
        invokeStartOverHandlers()
    }

    fun setup() {
    }

    fun cleanup() {
        stopAlarm()
    }

    private fun updateAlarmFireTime() {
        if (minutes <= 0) {
            Log.e("", "Setup alarm with invalid time $minutes")
            return
        }
        val delayMillis = minutes.toLong() * 60 * 1000L
        alarmData?.alarmRuntimeData?.setAlarmFireTime(System.currentTimeMillis() + delayMillis)
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d("", "firing alarm")
                invokeFireHandler()
            }
        }, delayMillis)
    }

    private fun invokeFireHandler() {
        for (h in alarmFireHandlers) {
            h.onAlarmFire(this)
        }
    }

    private fun invokeStartOverHandlers() {
        for (h in alarmStartOverHandlers) {
            h.onAlarmStartOver(this)
        }
    }

    interface AlarmFireHandler {
        fun onAlarmFire(sender: AlarmController)
    }

    interface AlarmStartOverHandler {
        fun onAlarmStartOver(sender: AlarmController)
    }
}