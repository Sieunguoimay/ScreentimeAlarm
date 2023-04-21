package com.sieunguoimay.screentimealarm.data

import android.content.Context
import com.sieunguoimay.screentimealarm.R

class AlarmDataController {
    private val alarmDataHandlers: ArrayList<AlarmDataHandler> = ArrayList()
    var alarmViewData: AlarmViewData? = null
        private set

    fun setAlarmData(alarmData: AlarmData) {
        alarmViewData = AlarmViewData(alarmData)
        invokeOnDataReady()
    }

    fun loadDataFromPersistent(context: Context): AlarmData {
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.common_shared_preferences),0
        )

        val maxScreenTime = sharedPref.getInt(context.getString(R.string.save_max_screen_time), 10)
        val alarmConfigData = AlarmConfigData()

        alarmConfigData.setMaxScreenTime(maxScreenTime)
        return AlarmData(alarmConfigData, AlarmRuntimeData())
    }

    fun savePersistentData(context: Context) {
        if (alarmViewData == null) return
        val sharedPref = context.getSharedPreferences(
            context.getString(R.string.common_shared_preferences), 0
        )
        sharedPref.edit().putInt(
            context.getString(R.string.save_max_screen_time),
            alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMinutes
        ).apply()
    }

    fun addHandler(handler: AlarmDataHandler) {
        alarmDataHandlers.add(handler)
    }

    private fun invokeOnDataReady() {
        for (h in alarmDataHandlers) {
            h.onDataReady(this)
        }
    }
}

interface AlarmDataHandler {
    fun onDataReady(sender: AlarmDataController)
}