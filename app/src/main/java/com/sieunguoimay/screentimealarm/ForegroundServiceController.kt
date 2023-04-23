package com.sieunguoimay.screentimealarm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import com.sieunguoimay.screentimealarm.data.AlarmDataController

class ForegroundServiceController(
    private val context: Context,
    private val dataController: AlarmDataController
) : ProgressRunningUI.StartOverButtonClickHandler {
    private var backgroundService: ForegroundService? = null
    private var activeHandler: ArrayList<ServiceActiveHandler> = ArrayList()
    var isActive: Boolean = false
        private set
    private var bindingOnStart: Boolean = false

    fun tryBindingToTheService() {
//        setConnectionStatus(ConnectionStatus.NotConnectedOnStart)
        isActive = false
        invokeActiveHandlers()

//        loadSavedAlarmData()
        ForegroundService.bindService(context, connection)
    }

    fun onActivityDestroy() {
    }

    fun tryToggleService() {
        if (backgroundService == null) {
            bindingOnStart = true
            dataController.savePersistentData(context)
            ForegroundService.startService(context, connection)
        } else {
            ForegroundService.stopService(context, connection)
        }
    }

    fun addHandler(handler: ServiceActiveHandler) {
        activeHandler.add(handler)
    }

//    private fun setConnectionStatus(connectionStatus: ConnectionStatus) {
//        Log.d("", "$connectionStatus")
//        isActive =
//            connectionStatus == ConnectionStatus.ConnectedSecondTimeOn || connectionStatus == ConnectionStatus.ConnectedFirstTime

//        if (connectionStatus == ConnectionStatus.ConnectedFirstTime) {
////            val dataFromActivity = dataController.alarmViewData?.alarmData
//            val dataFromPersistent = dataController.loadDataFromPersistent(context)
////            dataController.setAlarmData(dataFromPersistent)
//
//            if (backgroundService != null) {
//                backgroundService!!.alarmController.setAlarmData(dataFromPersistent)
//                backgroundService!!.alarmController.startAlarm()
//                dataController.alarmViewData?.alarmController = backgroundService!!.alarmController
//                toastFirstTimeEnableService()
//            }
//        } else
//        if (connectionStatus == ConnectionStatus.ConnectedSecondTimeOn) {
//            val dataFromService = backgroundService?.alarmController?.alarmData
//            if (dataFromService != null) {
//                dataController.setAlarmData(dataFromService)
//                dataController.alarmViewData?.alarmController = backgroundService?.alarmController
//            }
//        } else
//        if (connectionStatus == ConnectionStatus.NotConnectedOnStart) {
//            val dataFromPersistent = AlarmDataController.loadDataFromPersistent(context)
//            dataController.setAlarmData(dataFromPersistent)
//        } else if (connectionStatus == ConnectionStatus.Disconnected) {
//
//        }

//        dataController.alarmViewData?.alarmData?.alarmRuntimeData?.setAlarmActiveStatus(isActive)
//
//        for (l in activeHandler) {
//            l.onConnectionStatusChanged(this@ForegroundServiceController)
//        }
//    }

    //    private fun onConnectedForFirstTime(){
//        //            val dataFromActivity = dataController.alarmViewData?.alarmData
//        val dataFromPersistent = AlarmDataController.loadDataFromPersistent(context)
////            dataController.setAlarmData(dataFromPersistent)
//
//        if (backgroundService != null) {
//            backgroundService!!.alarmController.setAlarmData(dataFromPersistent)
//            backgroundService!!.alarmController.startAlarm()
//            toastFirstTimeEnableService(dataFromPersistent)
//        }
//    }
//    private fun toastFirstTimeEnableService(dataFromPersistent: AlarmData?){
//        val time =
//            if (dataFromPersistent != null)
//                AlarmViewData.formatTime(dataFromPersistent.alarmConfigData.maxScreenTimeMilliSeconds)
//            else "null"
//        Toast.makeText(
//            context,
//            String.format(backgroundService!!.getString(R.string.begin_toast), time),
//            Toast.LENGTH_LONG
//        ).show()
//    }
//
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

            if (bindingOnStart) {
//                setConnectionStatus(ConnectionStatus.ConnectedFirstTime)
                backgroundService?.setupAlarm(dataController.alarmViewData!!.alarmData)
                dataController.alarmViewData?.alarmController = backgroundService?.alarmController
                bindingOnStart = false
            } else {
//                setConnectionStatus(ConnectionStatus.ConnectedSecondTimeOn)
                takeAlarmDataFromRunningService()
            }
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
//            setConnectionStatus(ConnectionStatus.Disconnected)
            isActive = false
            invokeActiveHandlers()
        }
    }

    interface ServiceActiveHandler {
        fun onConnectionStatusChanged(sender: ForegroundServiceController)
    }

//    enum class ConnectionStatus {
        //        ConnectedFirstTime,
//        ConnectedSecondTimeOn,
//        NotConnectedOnStart,
//        Disconnected
//    }

    override fun onStartOverClicked() {
        backgroundService?.alarmController?.startOver()
    }
}