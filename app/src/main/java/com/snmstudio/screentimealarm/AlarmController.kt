package com.snmstudio.screentimealarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.snmstudio.screentimealarm.data.AlarmData
import java.util.*

class AlarmController() {

    var alarmData: AlarmData? = null
        private set

    private var isRunning: Boolean = false

    val alarmFireHandlers: ArrayList<AlarmFireHandler> = ArrayList()
    val alarmStartOverHandlers: ArrayList<AlarmStartOverHandler> = ArrayList()
    val alarmStopHandlers: ArrayList<AlarmStopHandler> = ArrayList()

    private val minutes: Int get() = alarmData?.alarmConfigData?.maxScreenTimeMinutes ?: 0

    private var timer = Timer()

    private var alarmManager: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent
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
//        startSystemAlarm(context)
    }

    fun stopAlarm() {
        isRunning = false
        timer.cancel()
        invokeStopHandlers()
//        stopSystemAlarm()
    }

    fun startOver() {
        isRunning = true
        timer.cancel()
        updateAlarmFireTime()
//        stopSystemAlarm()
//        startSystemAlarm(context)
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
                invokeFireHandlers()
            }
        }, delayMillis)
    }

    private fun getTriggerTime(): Long {
        val delayMillis = minutes.toLong() * 60 * 1000L
        return System.currentTimeMillis() + delayMillis
    }

    private fun invokeFireHandlers() {
        for (h in alarmFireHandlers) {
            h.onAlarmFire(this)
        }
    }

    private fun invokeStartOverHandlers() {
        for (h in alarmStartOverHandlers) {
            h.onAlarmStartOver(this)
        }
    }

    private fun invokeStopHandlers() {
        for (h in alarmStopHandlers) {
            h.onAlarmStop(this)
        }
    }

    interface AlarmFireHandler {
        fun onAlarmFire(sender: AlarmController)
    }

    interface AlarmStartOverHandler {
        fun onAlarmStartOver(sender: AlarmController)
    }

    interface AlarmStopHandler {
        fun onAlarmStop(sender: AlarmController)
    }

    private fun startSystemAlarm(context: Context) {
        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "Alarms"

        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val delayMillis = minutes.toLong() * 60 * 1000L

        alarmManager?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delayMillis,
            alarmIntent
        )
    }

    private fun stopSystemAlarm() {
        alarmManager?.cancel(alarmIntent)
    }

    class AlarmReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            staticAlarmController?.invokeFireHandlers()
        }

        companion object {
            var staticAlarmController: AlarmController? = null
        }
    }
}