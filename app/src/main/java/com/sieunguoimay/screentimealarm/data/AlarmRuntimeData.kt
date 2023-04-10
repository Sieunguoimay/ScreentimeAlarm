package com.sieunguoimay.screentimealarm.data

import android.util.Log

class AlarmRuntimeData {
    var alarmActiveStatus: Boolean = false
        private set
    var foregroundServiceActiveStatus: Boolean = false
        private set

    val changeHandler: ArrayList<AlarmRuntimeDataChangeHandler> = ArrayList()

    private fun invokeOnChange() {
        for (h in changeHandler) {
            h.onChange(this)
        }
    }

    fun setAlarmActiveStatus(active: Boolean) {
        Log.d("","setAlarmActiveStatus")
        alarmActiveStatus = active
        invokeOnChange()
    }

    fun setForegroundServiceActiveStatus(active: Boolean) {
        foregroundServiceActiveStatus = active
        invokeOnChange()
    }
}

interface AlarmRuntimeDataChangeHandler {
    fun onChange(data: AlarmRuntimeData)
}