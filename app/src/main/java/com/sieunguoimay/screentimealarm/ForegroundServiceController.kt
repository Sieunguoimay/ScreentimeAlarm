package com.sieunguoimay.screentimealarm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.sieunguoimay.screentimealarm.data.AlarmData
import com.sieunguoimay.screentimealarm.data.AlarmDataController

class ForegroundServiceController(val context: Context,val dataController: AlarmDataController) {
    private var backgroundService: ForegroundService? = null
    private var lockButtonFlag: Boolean = false
    private var activeHandler: List<ServiceActiveHandler> = emptyList()
    private var isActive: Boolean = false
    private var bindingOnStart:Boolean = false

    fun tryBindingToTheService() {
        if (ForegroundService.bindService(context, connection)) {
//            lockTheButton()
        } else {
            setActive(false)
        }
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
        activeHandler.plus(handler)
    }

    private fun lockTheButton() {
        lockButtonFlag = true
    }

    private fun unlockTheButton() {
        lockButtonFlag = false
    }

    private fun setActive(active: Boolean) {
        isActive = active
        for (l in activeHandler) {
            l.onServiceActiveChanged(this@ForegroundServiceController)
        }
    }

    fun getActive(): Boolean {
        return isActive
    }

    fun getAlarmData(): AlarmData? {
        return backgroundService?.alarmController?.alarmData
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            backgroundService = (service as ForegroundService.LocalBinder).getService()
            backgroundService?.serviceDestroyHandler = onServiceDestroyHandler

            if(bindingOnStart){
                onServiceConnectedFirstTime()
                bindingOnStart = false
            }

            // Do something with the service reference
            Log.d("", "onServiceConnected")
            unlockTheButton()
            setActive(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }

        private fun onServiceConnectedFirstTime(){
            backgroundService?.alarmController?.setAlarmData(dataController.alarmViewData?.alarmData)
            backgroundService?.alarmController?.startAlarm()
        }
    }

    private val onServiceDestroyHandler = object : ForegroundService.ServiceDestroyHandler {
        override fun onServiceDestroy(service: ForegroundService) {
            backgroundService = null
            unlockTheButton()
            Log.d("", "onServiceDestroy")
            setActive(false)
        }
    }

    interface ServiceActiveHandler {
        fun onServiceActiveChanged(sender: ForegroundServiceController)
    }
}