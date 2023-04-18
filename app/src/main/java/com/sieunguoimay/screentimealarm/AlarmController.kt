package com.sieunguoimay.screentimealarm

import android.app.Service
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.sieunguoimay.screentimealarm.data.AlarmData
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.*
import kotlin.coroutines.coroutineContext

class AlarmController() {

    var alarmData: AlarmData? = null
        private set

    private var isRunning: Boolean = false

    val alarmFireHandlers: ArrayList<AlarmFireHandler> = ArrayList()

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
        Log.d("", "stopAlarm")
        isRunning = false
        timer.cancel()
    }

    fun startOver() {
        isRunning = true
        timer.cancel()
        updateAlarmFireTime()
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
        Log.d("", "setupAlarm $minutes")

        val delayMillis = minutes.toLong() * 1000L
        alarmData?.alarmRuntimeData?.setAlarmFireTime(System.currentTimeMillis() + delayMillis)
        timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                Log.d("", "firing alarm")
                invokeHandler()
            }
        }, delayMillis)
    }

    private fun invokeHandler() {
        for (h in alarmFireHandlers) {
            h.onAlarmFire(this)
        }
    }

    interface AlarmFireHandler {
        fun onAlarmFire(sender: AlarmController)
    }
}