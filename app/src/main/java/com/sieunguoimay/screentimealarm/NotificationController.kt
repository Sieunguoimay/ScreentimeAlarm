package com.sieunguoimay.screentimealarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class NotificationController(private var service: Service) {

    private val notificationChannelId = "ENDLESS SERVICE CHANNEL"
    private val notificationId: Int = 20
    private lateinit var notification: Notification

    fun show() {
        notification = createNotification()
        service.startForeground(notificationId, notification)
    }

    fun dropDown() {
        // Update the existing notification using NotificationManager
        val notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    fun dismiss() {
        val notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }


    private fun createNotification(): Notification {

        setupNotificationChannel()
        val builder = createNotificationBuilder()
        val contentView = createNotificationView();
        val pendingIntent = createPendingIntent()

        return builder
//            .setContentTitle("Endless Service")
//            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setTicker("Ticker text")
            .setCustomContentView(contentView)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                notificationChannelId, "Endless Service notifications channel",
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
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            NotificationCompat.Builder(service, notificationChannelId)
        else NotificationCompat.Builder(service)
    }

    private fun createNotificationView(): RemoteViews {
        val contentView = RemoteViews(service.packageName, R.layout.custom_notification_layout)
        contentView.setTextViewText(R.id.titleTextView, "Title Text")
        return contentView
    }

    private fun createPendingIntent(): PendingIntent {
        return Intent(service, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(service, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}