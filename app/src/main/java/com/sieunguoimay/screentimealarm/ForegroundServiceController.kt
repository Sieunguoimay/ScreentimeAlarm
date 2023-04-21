package com.sieunguoimay.screentimealarm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.sieunguoimay.screentimealarm.data.AlarmDataController
import com.sieunguoimay.screentimealarm.data.AlarmViewData

class ForegroundServiceController(
    private val context: Context,
    private val dataController: AlarmDataController
) {
    private var backgroundService: ForegroundService? = null
    private var lockButtonFlag: Boolean = false
    private var activeHandler: ArrayList<ServiceActiveHandler> = ArrayList()
    var isActive: Boolean = false
        private set
    private var bindingOnStart: Boolean = false

    fun tryBindingToTheService() {
        setConnectionStatus(ConnectionStatus.NotConnectedOnStart)
        ForegroundService.bindService(context, connection)
        Log.d("", "tryBindingToTheService")
    }

    fun onActivityDestroy() {

    }

    fun tryToggleService() {
        if (!lockButtonFlag) {
            lockTheButton()
            if (backgroundService == null) {
                bindingOnStart = true
                ForegroundService.startService(context, connection)
            } else {
                ForegroundService.stopService(context, connection)
            }
        }
    }

    fun addHandler(handler: ServiceActiveHandler) {
        activeHandler.add(handler)
    }

    private fun lockTheButton() {
        lockButtonFlag = true
    }

    private fun unlockTheButton() {
        lockButtonFlag = false
    }

    private fun setConnectionStatus(connectionStatus: ConnectionStatus) {
        Log.d("", "$connectionStatus")
        isActive =
            connectionStatus == ConnectionStatus.ConnectedSecondTimeOn || connectionStatus == ConnectionStatus.ConnectedFirstTime

        if (connectionStatus == ConnectionStatus.ConnectedFirstTime) {
            val dataFromActivity = dataController.alarmViewData?.alarmData
            if (dataFromActivity != null && backgroundService != null) {
                backgroundService!!.alarmController.setAlarmData(dataFromActivity)
                backgroundService!!.alarmController.startAlarm()
                dataController.alarmViewData?.alarmController = backgroundService!!.alarmController
                val time =
                    if (dataController.alarmViewData != null)
                        AlarmViewData.formatTime(dataController.alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMilliSeconds)
                    else "null"
                Toast.makeText(
                    context,
                    String.format(backgroundService!!.getString(R.string.begin_toast), time),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (connectionStatus == ConnectionStatus.ConnectedSecondTimeOn) {
            val dataFromService = backgroundService?.alarmController?.alarmData
            if (dataFromService != null) {
                dataController.setAlarmData(dataFromService)
                dataController.alarmViewData?.alarmController = backgroundService?.alarmController
            }
        } else if (connectionStatus == ConnectionStatus.NotConnectedOnStart) {
            val dataFromPersistent = dataController.loadDataFromPersistent()
            dataController.setAlarmData(dataFromPersistent)
        } else if (connectionStatus == ConnectionStatus.Disconnected) {

        }

        dataController.alarmViewData?.alarmData?.alarmRuntimeData?.setAlarmActiveStatus(isActive)

        for (l in activeHandler) {
            l.onConnectionStatusChanged(this@ForegroundServiceController)
        }
    }


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            backgroundService = (service as ForegroundService.LocalBinder).getService()
            backgroundService?.serviceDestroyHandler = onServiceDestroyHandler

            if (bindingOnStart) {
                setConnectionStatus(ConnectionStatus.ConnectedFirstTime)
                bindingOnStart = false
            } else {
                setConnectionStatus(ConnectionStatus.ConnectedSecondTimeOn)
            }

            // Do something with the service reference
            Log.d("", "onServiceConnected")
            unlockTheButton()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    private val onServiceDestroyHandler = object : ForegroundService.ServiceDestroyHandler {
        override fun onServiceDestroy(service: ForegroundService) {
            backgroundService = null
            unlockTheButton()
            Log.d("", "onServiceDestroy")
            setConnectionStatus(ConnectionStatus.Disconnected)
        }
    }

    interface ServiceActiveHandler {
        fun onConnectionStatusChanged(sender: ForegroundServiceController)
    }

    enum class ConnectionStatus {
        ConnectedFirstTime,
        ConnectedSecondTimeOn,
        NotConnectedOnStart,
        Disconnected
    }
}