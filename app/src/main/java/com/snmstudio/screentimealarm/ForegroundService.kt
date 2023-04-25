package com.snmstudio.screentimealarm

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
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.snmstudio.screentimealarm.data.AlarmData
import com.snmstudio.screentimealarm.data.AlarmDataController
import com.snmstudio.screentimealarm.data.AlarmViewData
import com.snmstudio.screentimealarm.notification.NotificationController


class ForegroundService : Service() {

    private var isActive = false
    private val screenStateReceiver = ScreenStateReceiver()
    private lateinit var notificationController: NotificationController

    var serviceDestroyHandler: ServiceDestroyHandler? = null
    val alarmController = AlarmController()

    override fun onBind(p0: Intent?): IBinder {
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        alarmController.setup()
        alarmController.alarmFireHandlers.add(alarmFireHandler)
        alarmController.alarmStartOverHandlers.add(alarmStartOverHandler)
        alarmController.alarmStopHandlers.add(alarmStopHandler)
        notificationController = NotificationController(this, alarmController)
        notificationController.show()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Check if the service is active
        if (!isActive) {
            isActive = true
            setupAlarm()
            startBackgroundTask()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        isActive = false
        unregisterReceiver(screenStateReceiver)
        alarmController.alarmFireHandlers.remove(alarmFireHandler)
        alarmController.alarmStartOverHandlers.remove(alarmStartOverHandler)
        alarmController.alarmStopHandlers.remove(alarmStopHandler)
        alarmController.cleanup()
        screenStateReceiver.onUnregister()
        notificationController.dismiss()
        serviceDestroyHandler?.onServiceDestroy(this)
        stopNoise()
    }

    private fun setupAlarm() {
        val dataFromPersistent = AlarmDataController.loadDataFromPersistent(this)
        alarmController.setAlarmData(dataFromPersistent)
        alarmController.startAlarm()
        toastFirstTimeEnableService(dataFromPersistent)
    }

    private fun toastFirstTimeEnableService(dataFromPersistent: AlarmData?) {
        val time =
            if (dataFromPersistent != null)
                AlarmViewData.formatTime(dataFromPersistent.alarmConfigData.maxScreenTimeMilliSeconds)
            else "null"
        Toast.makeText(
            this,
            String.format(getString(R.string.begin_toast), time),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun startBackgroundTask() {
        // Register the BroadcastReceiver to receive the screen state intents
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, intentFilter)
        screenStateReceiver.onRegister(alarmController)
    }

    private val alarmFireHandler = object : AlarmController.AlarmFireHandler {
        override fun onAlarmFire(sender: AlarmController) {
            notificationController.dropDownHeadTop()
            makeNoise()
        }
    }
    private val alarmStartOverHandler = object : AlarmController.AlarmStartOverHandler {
        override fun onAlarmStartOver(sender: AlarmController) {
            stopNoise()
        }
    }
    private val alarmStopHandler = object : AlarmController.AlarmStopHandler {
        override fun onAlarmStop(sender: AlarmController) {
            stopNoise()
        }
    }

    private fun makeNoise() {
        tryVibrate()
    }

    private fun stopNoise() {
        stopVibrate()
    }

    private fun tryVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val effect = createEffect()
            vibratorManager.vibrate(CombinedVibration.createParallel(effect!!))
        } else {

            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = createEffect()
                    vibrator.cancel()
                    vibrator.vibrate(effect)
                } else {
                    vibrator.vibrate(getPattern(), 0)
                }
            }
        }
    }

    private fun stopVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.cancel()
        } else {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.cancel()
        }
    }

    private fun getPattern(): LongArray {
        return longArrayOf(1000, 300, 1000, 300)
    }

    private fun createEffect(): VibrationEffect? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val amplitudes =
                intArrayOf(
                    0,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                    0,
                    VibrationEffect.DEFAULT_AMPLITUDE,
                )
            VibrationEffect.createWaveform(
                getPattern(), amplitudes,
                0
            )
        } else {
            null
        }
    }

    companion object {

        fun startService(context: Context) {
            val intent = Intent(context, ForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun bindService(context: Context, connection: ServiceConnection): Boolean {
            val intent = Intent(context, ForegroundService::class.java)
            return context.bindService(intent, connection, 0)
        }

        fun stopService(context: Context, connection: ServiceConnection) {
            val intent = Intent(context, ForegroundService::class.java)
            context.unbindService(connection)
            context.stopService(intent)
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