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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class ForegroundService : Service() {

    private val screenStateReceiver = ScreenStateReceiver()
    private var isActive = false

    var serviceDestroyHandler: ServiceDestroyHandler? = null
    var activeStateHandler: ActiveStateHandler? = null
    val alarmController = AlarmController()
    private lateinit var notification: Notification
    private var NOTIFICATION_ID: Int = 20

    override fun onBind(p0: Intent?): IBinder {
        Log.d("", "onBind")
        return LocalBinder()
    }

    override fun onCreate() {
        super.onCreate()
        alarmController.setupDependencies(applicationContext, alarmFireHandler)
        notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
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

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

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
            // Update the existing notification using NotificationManager
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
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


    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) NotificationCompat.Builder(
                this,
                notificationChannelId
            ) else NotificationCompat.Builder(this)
        // Create a custom remote views for the notification
        val contentView = RemoteViews(packageName, R.layout.custom_notification_layout)
        // Set up the appearance of the notification using the RemoteViews
        contentView.setTextViewText(R.id.titleTextView, "Title Text")

// Set the notification to appear as a heads-up notification
        builder.setFullScreenIntent(pendingIntent, true);

        return builder
//            .setContentTitle("Endless Service")
//            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setTicker("Ticker text")
            .setCustomContentView(contentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}