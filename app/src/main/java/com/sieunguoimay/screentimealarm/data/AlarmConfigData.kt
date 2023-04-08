package com.sieunguoimay.screentimealarm.data

class AlarmConfigData {

    private var maxScreenTime: Float = 0f
    private var playSoundOnAlarm: Boolean = false
    private var vibrateOnAlarm: Boolean = false
    private var useCustomMessageOnAlarm: Boolean = false
    private var customMessage: String = ""
    private val changeHandler: List<AlarmConfigDataChangeHandler> = emptyList()

    private fun invokeOnChanged() {
        for (h in changeHandler) {
            h.onChanged(this)
        }
    }

    fun setMaxScreenTime(time: Float) {
        maxScreenTime = time
        invokeOnChanged()
    }

    fun getMaxScreenTime(): Float {
        return maxScreenTime
    }

    fun setPlaySoundOnAlarm(active: Boolean) {
        playSoundOnAlarm = active
        invokeOnChanged()
    }

    fun getPlaySoundOnAlarm(): Boolean {
        return playSoundOnAlarm;
    }

    fun setVibration(active: Boolean) {
        vibrateOnAlarm = active
        invokeOnChanged()
    }

    fun getVibration(): Boolean {
        return vibrateOnAlarm
    }

    fun setUseCustomMessageOnAlarm(active: Boolean) {
        useCustomMessageOnAlarm = active
        invokeOnChanged()
    }

    fun getUseCustomMessageOnAlarm(): Boolean {
        return useCustomMessageOnAlarm
    }

    fun setCustomMessage(message: String) {
        customMessage = message;
        invokeOnChanged()
    }

    fun getCustomMessage(): String {
        return customMessage
    }
}

interface AlarmConfigDataChangeHandler {
    fun onChanged(data: AlarmConfigData)
}