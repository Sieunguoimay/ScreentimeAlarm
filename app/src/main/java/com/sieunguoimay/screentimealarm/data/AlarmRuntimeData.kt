package com.sieunguoimay.screentimealarm.data

class AlarmRuntimeData() {
    private var alarmActiveStatus: Boolean = false
    private var foregroundServiceActiveStatus: Boolean = false
    private val changeHandler: List<AlarmRuntimeDataChangeHandler> = emptyList()
    private fun invokeOnChange() {
        for (h in changeHandler) {
            h.onChange(this)
        }
    }

    fun setAlarmActiveStatus(active: Boolean) {
        alarmActiveStatus = active
        invokeOnChange()
    }

    fun getAlarmActiveStatus(): Boolean {
        return alarmActiveStatus
    }

    fun setForegroundServiceActiveStatus(active: Boolean) {
        foregroundServiceActiveStatus = active
        invokeOnChange()
    }

    fun getForegroundServiceActiveStatus(): Boolean {
        return foregroundServiceActiveStatus
    }
}

interface AlarmRuntimeDataChangeHandler {
    fun onChange(data: AlarmRuntimeData)
}