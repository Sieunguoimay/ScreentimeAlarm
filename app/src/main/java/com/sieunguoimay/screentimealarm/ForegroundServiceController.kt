package com.sieunguoimay.screentimealarm

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.sieunguoimay.screentimealarm.data.AlarmDataController


class ForegroundServiceController(
    private val context: Context,
    private val dataController: AlarmDataController
) : ProgressRunningUI.StartOverButtonClickHandler {
    private var backgroundService: ForegroundService? = null
    private var activeHandler: ArrayList<ServiceActiveHandler> = ArrayList()
    var isActive: Boolean = false
        private set

    fun tryBindingToTheService() {
        isActive = false
        invokeActiveHandlers()
        ForegroundService.bindService(context, connection)
    }

    fun tryToggleService() {
        if (backgroundService == null) {
            dataController.savePersistentData(context)
            ForegroundService.startService(context)
            ForegroundService.bindService(context, connection)
        } else {
            ForegroundService.stopService(context, connection)
        }
    }

    fun addHandler(handler: ServiceActiveHandler) {
        activeHandler.add(handler)
    }

    private fun invokeActiveHandlers() {
        for (l in activeHandler) {
            l.onConnectionStatusChanged(this)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isActive = true

            backgroundService = (service as ForegroundService.LocalBinder).getService()
            backgroundService?.serviceDestroyHandler = onServiceDestroyHandler

            takeAlarmDataFromRunningService()
            invokeActiveHandlers()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private fun takeAlarmDataFromRunningService() {
        val dataFromService = backgroundService?.alarmController?.alarmData
        if (dataFromService != null) {
            dataController.setAlarmData(dataFromService)
            dataController.alarmViewData?.alarmController = backgroundService?.alarmController
        }
    }

    private val onServiceDestroyHandler = object : ForegroundService.ServiceDestroyHandler {
        override fun onServiceDestroy(service: ForegroundService) {
            backgroundService = null
            isActive = false
            invokeActiveHandlers()
        }
    }

    interface ServiceActiveHandler {
        fun onConnectionStatusChanged(sender: ForegroundServiceController)
    }

    override fun onStartOverClicked() {
        backgroundService?.alarmController?.startOver()
    }

    fun onActivityDestroy() {

    }
}