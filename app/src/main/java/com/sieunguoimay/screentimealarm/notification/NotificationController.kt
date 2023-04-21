package com.sieunguoimay.screentimealarm.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sieunguoimay.screentimealarm.AlarmController
import com.sieunguoimay.screentimealarm.MainActivity
import com.sieunguoimay.screentimealarm.R


class NotificationController(
    private val service: Service,
    private val alarmController: AlarmController,
) : AlarmController.AlarmStartOverHandler {

    private val alarmChannelId = "Alarm Channel ID"
    private val progressChannelId = "Progress Channel ID"
    private val notificationId: Int = 20
    private var notificationManager: NotificationManager? = null
    private var threadRunning: Boolean = false

    fun show() {
        createNotification()
        remoteViewController?.createNotificationForFirstTime()
        service.startForeground(notificationId, remoteViewController?.notification)
        alarmController.alarmStartOverHandlers.add(this)
        startThread()
    }

    fun dropDown() {
        stopThread()
        remoteViewController?.createNotificationForDropDown()
        notificationManager?.notify(notificationId, remoteViewController?.notification)
    }

    override fun onAlarmStartOver(sender: AlarmController) {
        startThread()
    }

    fun dismiss() {
        notificationManager?.cancel(notificationId)
        alarmController.alarmStartOverHandlers.remove(this)
        stopThread()
    }

    private fun startThread() {
        threadRunning = true
        Thread {
            while (threadRunning) {
                remoteViewController?.updateProgress()
                notificationManager?.notify(notificationId, remoteViewController?.notification)
                SystemClock.sleep(1000)
            }
        }.start()
    }

    private fun stopThread() {
        threadRunning = false
    }

    private fun createNotification() {
        setupNotificationChannel()
        val progressBuilder = createProgressNotificationBuilder()
        val alarmBuilder = createAlarmNotificationBuilder()
        val contentBigView = createBigNotificationView();
        val contentSmallView = createSmallNotificationView()
        val stringProvider = createStringProvider()
        notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        remoteViewController = RemoteViewsController(
            contentBigView, contentSmallView,
            alarmBuilder, progressBuilder, stringProvider, alarmController
//                Icon.createWithResource(service, R.drawable.ico_drop_down),
//                Icon.createWithResource(service, R.drawable.ico_drop_up)
        )
    }

    private fun createStringProvider(): RemoteViewsController.ResourceProvider {
        return RemoteViewsController.ResourceProvider(
            service.getString(R.string.alarm_goes_off),
            service.getColor(R.color.progress),
            service.getColor(R.color.progress_red)
        )
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
            val notificationManager =
                service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                alarmChannelId, "Alarm channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it
            }

            val channel2 = NotificationChannel(
                progressChannelId, "Progress Channel",
                NotificationManager.IMPORTANCE_LOW
            ).let {
                it.description = "Endless Service channel"
                it
            }

            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(channel2)
        }
    }

    private fun createAlarmNotificationBuilder(): NotificationCompat.Builder {
        val pendingIntent = createContentPendingIntent()
        return (if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            NotificationCompat.Builder(service, alarmChannelId)
        else NotificationCompat.Builder(service))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
//            .setContentTitle("Endless Service")
//            .setContentText("This is your favorite endless service working")
            .setSmallIcon(R.drawable.ico_eye_drop)
//            .setTicker("Ticker text")
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)// for under android 26 compatibility
            .setShowWhen(false)
    }

    private fun createProgressNotificationBuilder(): NotificationCompat.Builder {
        val pendingIntent = createContentPendingIntent()
        return (if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            NotificationCompat.Builder(service, progressChannelId)
        else NotificationCompat.Builder(service))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, false)
            .setSmallIcon(R.drawable.ico_eye_drop)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setShowWhen(false)
    }

    private fun createBigNotificationView(): RemoteViews {
        val contentView = RemoteViews(service.packageName, R.layout.layout_notification_big)
        contentView.setOnClickPendingIntent(
            R.id.button_start_over,
            createRemoteViewPendingIntent(R.id.button_start_over, 0)
        )
        return contentView
    }

    private fun createSmallNotificationView(): RemoteViews {
        return RemoteViews(service.packageName, R.layout.layout_notification_small)
    }

    private fun createContentPendingIntent(): PendingIntent {
        return Intent(service, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(
                service, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    private fun createRemoteViewPendingIntent(viewId: Int, code: Int): PendingIntent {
        val intent = Intent(service, RemoteViewBroadcastReceiver::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.action = RemoteViewBroadcastReceiver.ACTION_NAME
        intent.putExtra("view_id", viewId)
        return PendingIntent.getBroadcast(service, code, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        var remoteViewController: RemoteViewsController? = null
    }

    class RemoteViewBroadcastReceiver : BroadcastReceiver() {
        companion object {
            const val ACTION_NAME = "RemoteViewsClicked"
        }

        override fun onReceive(context: Context, intent: Intent) {
            val viewId = intent.getIntExtra("view_id", -1)
            if (viewId == R.id.button_start_over) {
                remoteViewController?.startOver()
            }
        }
    }
}
