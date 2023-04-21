package com.sieunguoimay.screentimealarm

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.CombinedVibration
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat


class ForegroundService : Service() {

    private var isActive = false
    private val screenStateReceiver = ScreenStateReceiver()
    private lateinit var notificationController: NotificationController

    var serviceDestroyHandler: ServiceDestroyHandler? = null
    val alarmController = AlarmController()

    private val vibratorManager: VibratorManager by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        } else {
            TODO("VERSION.SDK_INT < S")
        }
    }
    override fun onBind(p0: Intent?): IBinder {
        Log.d("", "onBind")
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        alarmController.setup()
        alarmController.alarmFireHandlers.add(alarmFireHandler)
        notificationController = NotificationController(this, alarmController)
        notificationController.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Check if the service is active
        if (!isActive) {
            isActive = true
            startBackgroundTask()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        unregisterReceiver(screenStateReceiver)
        alarmController.alarmFireHandlers.remove(alarmFireHandler)
        alarmController.cleanup()
        screenStateReceiver.onUnregister(applicationContext)
        notificationController.dismiss()
        serviceDestroyHandler?.onServiceDestroy(this)
    }

    private fun startBackgroundTask() {
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

    private fun isPrimitiveSupported(effectId: Int): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager.defaultVibrator.areAllPrimitivesSupported(effectId)
        } else {
            TODO("VERSION.SDK_INT < S")
        }
    }
    private fun tryVibrate(effectId: Int) {
        if (isPrimitiveSupported(effectId)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                vibratorManager.vibrate(
                    CombinedVibration.createParallel(
                        VibrationEffect.startComposition()
                            .addPrimitive(effectId)
                            .compose()
                    )
                )
            }
        } else {
            Toast.makeText(
                this,
                "This primitive is not supported by this device.",
                Toast.LENGTH_LONG,
            ).show()
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
}