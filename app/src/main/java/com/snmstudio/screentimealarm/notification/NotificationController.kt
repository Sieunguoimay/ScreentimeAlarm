package com.snmstudio.screentimealarm.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sieunguoimay.screentimealarm.R
import com.snmstudio.screentimealarm.AlarmController
import com.snmstudio.screentimealarm.MainActivity


class NotificationController(
    private val service: Service,
    private val alarmController: AlarmController,
) : AlarmController.AlarmStartOverHandler, RemoteViewsController.ExtendHandler {

    private val alarmChannelId = "Alarm Channel ID"
    private val progressChannelId = "Progress Channel ID"
    private val notificationId: Int = 20
    private var notificationManager: NotificationManager? = null
    private var threadRunning: Boolean = false
    private var thread: Thread? = null

    fun show() {
        createNotification()
        remoteViewController?.createNotificationForFirstTime()
        service.startForeground(notificationId, remoteViewController?.notification)
        alarmController.alarmStartOverHandlers.add(this)

//        startThread()
    }

    fun dropDownHeadTop() {
        stopThread()
        remoteViewController?.createNotificationForDropDown()
        notificationManager?.notify(notificationId, remoteViewController?.notification)
    }

    override fun onAlarmStartOver(sender: AlarmController) {
        if (remoteViewController != null && remoteViewController!!.extending) {
            stopThread()
            startThread()
        }else{
            remoteViewController?.createLowPriorityNotification()
            service.startForeground(notificationId, remoteViewController?.notification)
        }
    }

    fun dismiss() {
        notificationManager?.cancel(notificationId)
        alarmController.alarmStartOverHandlers.remove(this)
        stopThread()
    }

    private fun startThread() {
        threadRunning = true
        thread = Thread {
            while (threadRunning) {
                remoteViewController?.updateProgress()
                notificationManager?.notify(notificationId, remoteViewController?.notification)
                try {
                    SystemClock.sleep(1000)
                } catch (e: InterruptedException) {
                    // We've been interrupted: no more messages.
                    break
                }
            }
        }
        thread!!.start()
    }

    private fun stopThread() {
        threadRunning = false
        thread?.interrupt()
    }

    override fun onExtend() {
        startThread()
        remoteViewController?.updateProgress()
        notificationManager?.notify(notificationId, remoteViewController?.notification)
    }

    override fun onMinimize() {
        stopThread()
        remoteViewController?.createLowPriorityNotification()
        service.startForeground(notificationId, remoteViewController?.notification)
    }

    private fun createNotification() {
        setupNotificationChannel()
        val progressBuilder = createProgressNotificationBuilder()
        val lowPriorityBuilder = createLowPriorityNotificationBuilder()
        val alarmBuilder = createAlarmNotificationBuilder()
        val contentBigView = createBigNotificationView();
        val contentSmallView = createSmallNotificationView()
        val stringProvider = createStringProvider()
        notificationManager =
            service.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        remoteViewController = RemoteViewsController(
            contentBigView, contentSmallView, lowPriorityBuilder,
            alarmBuilder, progressBuilder, stringProvider, alarmController, this
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

    private fun createLowPriorityNotificationBuilder(): NotificationCompat.Builder {
        val pendingIntent = createContentPendingIntent()
        return (if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            NotificationCompat.Builder(service, progressChannelId)
        else NotificationCompat.Builder(service))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ico_eye_drop)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setPriority(NotificationCompat.PRIORITY_LOW)// for under android 26 compatibility
            .setShowWhen(false)
    }

    private fun createProgressNotificationBuilder(): NotificationCompat.Builder {
        val pendingIntent = createContentPendingIntent()
        return (if (Build.VERSION.SDK_INT >= VERSION_CODES.O)
            NotificationCompat.Builder(service, progressChannelId)
        else NotificationCompat.Builder(service))
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
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
        contentView.setOnClickPendingIntent(
            R.id.button_collapse,
            createRemoteViewPendingIntent(R.id.button_collapse, 2)
        )
        return contentView
    }

    private fun createSmallNotificationView(): RemoteViews {
        val view = RemoteViews(service.packageName, R.layout.layout_notification_small)
        view.setOnClickPendingIntent(
            R.id.button_extends,
            createRemoteViewPendingIntent(R.id.button_extends, 1)
        )
        return view
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
                remoteViewController?.startOver(context)
            } else if (viewId == R.id.button_extends) {
                remoteViewController?.extends()
            } else if (viewId == R.id.button_collapse) {
                remoteViewController?.collapse()
            }
        }
    }

}
