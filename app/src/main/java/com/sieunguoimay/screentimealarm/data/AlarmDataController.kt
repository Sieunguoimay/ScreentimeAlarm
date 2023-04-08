package com.sieunguoimay.screentimealarm.data

import com.sieunguoimay.screentimealarm.ForegroundServiceController

class AlarmDataController(private val foregroundServiceController: ForegroundServiceController) {
    private val alarmDataHandlers: List<AlarmDataHandler> = emptyList()
    var alarmViewData: ViewData? = null
        private set

    fun setup() {
        foregroundServiceController.addHandler(serviceActiveHandler)
    }

    fun addHandler(handler: AlarmDataHandler) {
        alarmDataHandlers.plus(handler)
    }

    private val serviceActiveHandler = object : ForegroundServiceController.ServiceActiveHandler {
        override fun onServiceActiveChanged(sender: ForegroundServiceController) {
            if (sender.getActive()) {

                var data = sender.getAlarmData()
                if (data != null) {
                    alarmViewData = ViewData(data)
                    invokeOnDataReady()
                }
            } else {
                    alarmViewData = ViewData(AlarmData(AlarmConfigData(),AlarmRuntimeData()))
                    invokeOnDataReady()
            }
        }
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