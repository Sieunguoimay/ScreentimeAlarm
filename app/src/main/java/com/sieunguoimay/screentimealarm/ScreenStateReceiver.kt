package com.sieunguoimay.screentimealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenStateReceiver : BroadcastReceiver() {

    private var alarmController: AlarmController? = null
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_ON -> {
                onScreenOn()
            }
            Intent.ACTION_SCREEN_OFF -> {
                onScreenOff()
            }
        }
    }

    private fun onScreenOn() {
        alarmController?.startOver()
    }

    private fun onScreenOff() {
        alarmController?.stopAlarm()
    }

    fun onRegister(alarmController: AlarmController) {
        this.alarmController = alarmController
    }

    fun onUnregister() {
    }
}
