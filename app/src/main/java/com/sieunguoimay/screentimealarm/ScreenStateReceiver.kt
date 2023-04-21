package com.sieunguoimay.screentimealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class ScreenStateReceiver : BroadcastReceiver() {

    private var alarmController: AlarmController? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                // Screen is turned on
                onScreenOn(context)
            }
            Intent.ACTION_SCREEN_OFF -> {
                // Screen is turned off
                onScreenOff(context)
            }
        }
    }

    private fun onScreenOn(context: Context?) {
//        Toast.makeText(context, "Screen is turned on", Toast.LENGTH_SHORT).show()
//        Log.d("", "Screen is turned on")
        alarmController?.startAlarm()
    }

    private fun onScreenOff(context: Context?) {
//        Toast.makeText(context, "Screen is turned off", Toast.LENGTH_SHORT).show()
//        Log.d("", "Screen is turned off")
        alarmController?.stopAlarm()
    }

    fun onRegister(context: Context?, alarmController: AlarmController) {
        this.alarmController = alarmController
    }

    fun onUnregister(context: Context?) {
    }
}
