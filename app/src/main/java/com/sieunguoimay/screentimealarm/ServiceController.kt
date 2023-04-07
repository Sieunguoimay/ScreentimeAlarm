package com.sieunguoimay.screentimealarm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

class ServiceController(var context: Context, activeHandler: ServiceActiveHandler) {
    private var backgroundService: ForegroundService? = null
    private var lockButtonFlag: Boolean = false
    private var activeHandler: ServiceActiveHandler? = activeHandler
    private var isActive: Boolean = false
    private var minutes: Int = 1

    fun tryBindingToTheService() {
        if (ForegroundService.bindService(context, connection)) {
//            lockTheButton()
        }
    }

    fun tryToggleService() {
        if (!lockButtonFlag) {
            lockTheButton()
            if (backgroundService == null) {
                ForegroundService.startService(context, connection)
            } else {
                ForegroundService.stopService(context, connection)
            }
        }
    }

    private fun lockTheButton() {
        lockButtonFlag = true
    }

    private fun unlockTheButton() {
        lockButtonFlag = false
    }

    private fun setActive(active: Boolean) {
        isActive = active
        activeHandler?.onServiceActiveChanged(this@ServiceController)
    }

    fun getActive(): Boolean {
        return isActive
    }

    fun setAlarmTime(minutes: Int) {
        this.minutes = minutes
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            backgroundService = (service as ForegroundService.LocalBinder).getService()
            backgroundService?.serviceDestroyHandler = onServiceDestroyHandler
            backgroundService?.alarmController?.startAlarmWithTime(minutes)

            // Do something with the service reference
            Log.d("", "onServiceConnected")
            unlockTheButton()
            setActive(true)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
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
        fun onServiceActiveChanged(sender: ServiceController)
    }
}