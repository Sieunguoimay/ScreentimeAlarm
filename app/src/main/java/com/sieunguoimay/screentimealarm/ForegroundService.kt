package com.sieunguoimay.screentimealarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.ContentView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ForegroundService : Service() {

    private val screenStateReceiver = ScreenStateReceiver()
    private var isActive = false

    var serviceDestroyHandler: ServiceDestroyHandler? = null
    var activeStateHandler: ActiveStateHandler? = null
    val alarmController = AlarmController()
    private lateinit var notificationController: NotificationController
    override fun onBind(p0: Intent?): IBinder {
        Log.d("", "onBind")
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        alarmController.setupDependencies(applicationContext, alarmFireHandler)
        notificationController = NotificationController(this)
        notificationController.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("", "onStartCommand")

        // Check if the service is active
        if (!isActive) {
            setActive(true)
            startBackgroundTask()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("", "onDestroy")
        // Inactivate the service
        setActive(false)
        unregisterReceiver(screenStateReceiver)
        alarmController.stopAlarm()
        screenStateReceiver.onUnregister(applicationContext)

        notificationController.dismiss()

        serviceDestroyHandler?.onServiceDestroy(this)
    }

    fun getActive(): Boolean {
        return isActive
    }

    private fun setActive(active: Boolean) {
        isActive = active
        activeStateHandler?.onActiveChanged(this)
    }

    private fun startBackgroundTask() {
        // Add your background task logic here
        // Register the BroadcastReceiver to receive the screen state intents
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, intentFilter)
        screenStateReceiver.onRegister(applicationContext, alarmController)

        //        Thread {
//            while (isActive) {
//                // Do your task here
//            }
//        }.start()
    }

    private val alarmFireHandler = object : AlarmController.AlarmFireHandler {
        override fun onAlarmFire(sender: AlarmController) {
            notificationController.dropDown()
            Log.d("", "onAlarmFire")
        }
    }

    companion object {

        fun startService(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
            context.bindService(intent, connection, 0)
            Log.d("", "startService")
        }

        fun bindService(context: Context, connection: ServiceConnection): Boolean {
            Log.d("", "bindService")
            val intent = Intent(context, ForegroundService::class.java)
            return context.bindService(intent, connection, 0)
        }

        fun stopService(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, ForegroundService::class.java)
            context.unbindService(connection)
            context.stopService(intent)
            Log.d("", "stopService")
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): ForegroundService {
            return this@ForegroundService
        }
    }

    interface ServiceDestroyHandler {
        fun onServiceDestroy(service: ForegroundService)
    }

    interface ActiveStateHandler {
        fun onActiveChanged(service: ForegroundService)
    }

}