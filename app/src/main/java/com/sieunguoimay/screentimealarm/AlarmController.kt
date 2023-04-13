package com.sieunguoimay.screentimealarm

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.sieunguoimay.screentimealarm.data.AlarmData
import java.util.*

class AlarmController() {

    var alarmData: AlarmData? = null
        private set
    private var isRunning: Boolean = false
    val alarmFireHandlers: ArrayList<AlarmFireHandler> = ArrayList()

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val minutes: Int
        get() = alarmData?.alarmConfigData?.maxScreenTimeMinutes ?: 0

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
        handler.removeCallbacks(runnable)
    }

    fun setup() {
        val handlerThread = HandlerThread("MyForegroundServiceHandlerThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        runnable = Runnable {
            Log.d("", "firing alarm")
            invokeHandler()
        }
    }
    fun cleanup(){
        stopAlarm()
    }

    private fun invokeHandler() {

        for (h in alarmFireHandlers) {
            h.onAlarmFire(this)
        }
    }

    private fun updateAlarmFireTime() {
        if (minutes <= 0) {
            Log.e("", "Setup alarm with invalid time $minutes")
            return
        }
        Log.d("", "setupAlarm $minutes")

        // Schedule the event after the specified time (in milliseconds)
        val delayMillis = minutes.toLong() * 1000L
        handler.postDelayed(runnable, delayMillis)

        alarmData?.alarmRuntimeData?.setAlarmFireTime(System.currentTimeMillis() + delayMillis)
    }

    interface AlarmFireHandler {
        fun onAlarmFire(sender: AlarmController)
    }
}