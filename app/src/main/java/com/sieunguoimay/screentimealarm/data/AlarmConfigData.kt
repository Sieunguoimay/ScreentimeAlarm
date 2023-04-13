package com.sieunguoimay.screentimealarm.data

import android.util.Log

class AlarmConfigData {

    val maxScreenTimeMilliSeconds: Long
        get() = maxScreenTimeMinutes * 1000L

    var maxScreenTimeMinutes: Int = 0
        private set
    var playSoundOnAlarm: Boolean = false
        private set
    var vibrateOnAlarm: Boolean = false
        private set
    var useCustomMessageOnAlarm: Boolean = false
        private set
    val changeHandler: ArrayList<AlarmConfigDataChangeHandler> = ArrayList()
    var customMessage: String = ""
        private set

    private fun invokeOnChanged() {
        for (h in changeHandler) {
            h.onChanged(this)
        }
    }

    fun setMaxScreenTime(time: Int) {
        Log.d("", "setMaxScreenTime $time")
        maxScreenTimeMinutes = time
        invokeOnChanged()
    }

    fun setPlaySoundOnAlarm(active: Boolean) {
        playSoundOnAlarm = active
        invokeOnChanged()
    }


    fun setVibration(active: Boolean) {
        vibrateOnAlarm = active
        invokeOnChanged()
    }

    fun setUseCustomMessageOnAlarm(active: Boolean) {
        useCustomMessageOnAlarm = active
        invokeOnChanged()
    }

    fun setCustomMessage(message: String) {
        customMessage = message;
        invokeOnChanged()
    }
}

interface AlarmConfigDataChangeHandler {
    fun onChanged(data: AlarmConfigData)
}