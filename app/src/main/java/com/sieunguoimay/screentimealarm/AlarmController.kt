package com.sieunguoimay.screentimealarm

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.sieunguoimay.screentimealarm.data.AlarmData
import java.util.*

class AlarmController {

    var alarmData: AlarmData? = null
        private set
    private var isRunning: Boolean = false
    private var context: Context? = null
    private var alarmFireHandler: AlarmFireHandler? = null
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private val minutes: Int
        get() = alarmData?.alarmConfigData?.maxScreenTime ?: 0

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
        // Remove the callback from the handler when the service is destroyed
        handler.removeCallbacks(runnable)
    }

    fun setupDependencies(context: Context, alarmFireHandler: AlarmFireHandler) {
        this.context = context
        this.alarmFireHandler = alarmFireHandler
        // Initialize the handler and runnable on a separate thread
        val handlerThread = HandlerThread("MyForegroundServiceHandlerThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        runnable = Runnable {
            // Your logic to handle the scheduled event
            // This will be executed after the specified time
            Log.d("", "firing alarm")
            this.alarmFireHandler?.onAlarmFire(this)
        }
    }

    private fun updateAlarmFireTime() {
//        val alarmTime = Calendar.getInstance()
//        alarmTime.set(Calendar.HOUR_OF_DAY, 8) // Set the hour of the alarm
//        alarmTime.set(Calendar.MINUTE, 30) // Set the minute of the alarm
        if (minutes <= 0) {
            Log.e("", "Setup alarm with invalid time $minutes")
            return
        }
        Log.d("", "setupAlarm $minutes")

        // Schedule the event after the specified time (in milliseconds)
        val delayMillis = minutes.toLong() * 1000L
        // 1 minute (you can change this to your desired time)
        handler.postDelayed(runnable, delayMillis)

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE,minutes)
        alarmData?.alarmRuntimeData?.setAlarmFireTime(calendar.timeInMillis)
    }

    interface AlarmFireHandler {
        fun onAlarmFire(sender: AlarmController)
    }
}