package com.sieunguoimay.screentimealarm.data

import android.util.Log

class AlarmDataController {
    private val alarmDataHandlers: ArrayList<AlarmDataHandler> = ArrayList()
    var alarmViewData: AlarmViewData? = null
        private set

    fun setAlarmData(alarmData: AlarmData){
        Log.d("","setAlarmData ${alarmDataHandlers.count()}")
        alarmViewData = AlarmViewData(alarmData)
        invokeOnDataReady()
    }

    fun loadDataFromPersistent():AlarmData{
        val alarmConfigData = AlarmConfigData()
        alarmConfigData.setMaxScreenTime(10)
        return AlarmData(alarmConfigData, alarmRuntimeData = AlarmRuntimeData())
     }

    fun addHandler(handler: AlarmDataHandler) {
        alarmDataHandlers.add(handler)
        Log.d("","addHandler ${alarmDataHandlers.count()}")
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