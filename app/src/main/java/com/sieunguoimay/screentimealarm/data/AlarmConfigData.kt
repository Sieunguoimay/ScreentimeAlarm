package com.sieunguoimay.screentimealarm.data

import android.util.Log

class AlarmConfigData {

    var maxScreenTime: Int = 0
        private set
        get
    var playSoundOnAlarm: Boolean = false
        private set
        get
    var vibrateOnAlarm: Boolean = false
        private set
        get
    var useCustomMessageOnAlarm: Boolean = false
        private set
        get
    val changeHandler: ArrayList<AlarmConfigDataChangeHandler> = ArrayList()
        get
    var customMessage: String = ""
        private set
        get

    private fun invokeOnChanged() {
        for (h in changeHandler) {
            h.onChanged(this)
        }
    }

    fun setMaxScreenTime(time: Int) {
        Log.d("","setMaxScreenTime $time")
        maxScreenTime = time
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