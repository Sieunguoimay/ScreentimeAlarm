package com.sieunguoimay.screentimealarm

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat


class NotificationController(private var service: Service) {

    private val notificationChannelId = "ENDLESS SERVICE CHANNEL"
    private val notificationId: Int = 20
    private lateinit var notification: Notification
    private var notificationManager: NotificationManager? = null

    fun show() {
        val (n, contentView) = createNotification()
        notification = n

        service.startForeground(notificationId, notification)

        notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        remoteViewController =
            RemoteViewController(
                contentView, notificationManager, notification, notificationId,
                Icon.createWithResource(service, R.drawable.ico_drop_down),
                Icon.createWithResource(service, R.drawable.ico_drop_up)
            )
    }

    fun dropDown() {
        notificationManager?.notify(notificationId, notification)
    }

    fun dismiss() {
        notificationManager?.cancel(notificationId)
    }

    private fun createNotification(): Pair<Notification, RemoteViews> {

        setupNotificationChannel()
        val builder = createNotificationBuilder()
        val contentBigView = createBigNotificationView();
        val contentSmallView = createSmallNotificationView()
        val pendingIntent = createContentPendingIntent()
        return Pair(
            builder
//            .setContentTitle("Endless Service")
//            .setContentText("This is your favorite endless service working")
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, false)
                .setSmallIcon(R.mipmap.ic_launcher)
//            .setTicker("Ticker text")
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(contentSmallView)
                .setCustomBigContentView(contentBigView)
                .setPriority(NotificationCompat.PRIORITY_LOW) // for under android 26 compatibility
                .build(), contentBigView
        )
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
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
        return if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            NotificationCompat.Builder(service, notificationChannelId)
        else NotificationCompat.Builder(service)
    }

    private fun createBigNotificationView(): RemoteViews {
        val contentView = RemoteViews(service.packageName, R.layout.layout_notification_big)
        contentView.setTextViewText(R.id.titleTextView, "Big View")
        if (Build.VERSION.SDK_INT >= VERSION_CODES.S) {
            contentView.setViewVisibility(R.id.button_drop_down, View.GONE)
        } else {
            val remoteViewPendingIntent = createRemoteViewPendingIntent(R.id.button_drop_down)
            contentView.setOnClickPendingIntent(R.id.button_drop_down, remoteViewPendingIntent)
            contentView.setViewVisibility(R.id.layout_lower_part, View.GONE)
        }

        return contentView
    }

    private fun createSmallNotificationView(): RemoteViews {
        val contentView = RemoteViews(service.packageName, R.layout.layout_notification_small)
        contentView.setTextViewText(R.id.titleTextView, "Small View")
        return contentView
    }

    private fun createContentPendingIntent(): PendingIntent {
        return Intent(service, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                service, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private fun createRemoteViewPendingIntent(viewId: Int): PendingIntent {
        val intent = Intent(service, RemoteViewBroadcastReceiver::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.action = RemoteViewBroadcastReceiver.ACTION_NAME
        intent.putExtra("view_id", viewId)
        return PendingIntent.getBroadcast(service, 0, intent, 0)
    }

    companion object {
        var remoteViewController: RemoteViewController? = null
    }

    class RemoteViewController(
        private val remoteViews: RemoteViews,
        private val notificationManager: NotificationManager?,
        private val notification: Notification,
        private val notificationId: Int,
        private val dropDownIcon: Icon,
        private val dropUpIcon: Icon
    ) {
        private var lowerPartVisible = false
        fun toggleLowerPart() {
            lowerPartVisible = !lowerPartVisible
            updateLowerPart()
            updateRemoteViews()
        }

        private fun updateLowerPart() {
            remoteViews.setViewVisibility(
                R.id.layout_lower_part,
                if (lowerPartVisible) View.VISIBLE else View.GONE
            )
            remoteViews.setImageViewIcon(
                R.id.drop_down_icon,
                if (lowerPartVisible) dropUpIcon else dropDownIcon
            )
        }

        private fun updateRemoteViews() {
            notificationManager?.notify(notificationId, notification)
        }
    }

    class RemoteViewBroadcastReceiver : BroadcastReceiver() {
        companion object {
            const val ACTION_NAME = "RemoteViewsClicked"
        }

        override fun onReceive(context: Context, intent: Intent) {
            val viewId = intent.getIntExtra("view_id", -1)
            if (viewId == R.id.button_drop_down) {
                remoteViewController?.toggleLowerPart()
            }
        }
    }

}
