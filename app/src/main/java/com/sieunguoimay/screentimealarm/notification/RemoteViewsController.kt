package com.sieunguoimay.screentimealarm.notification

import android.app.Notification
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.sieunguoimay.screentimealarm.AlarmController
import com.sieunguoimay.screentimealarm.R
import com.sieunguoimay.screentimealarm.data.AlarmViewData


class RemoteViewsController(
    private val contentBigView: RemoteViews,
    private val contentSmallView: RemoteViews,
    private val alarmBuilder: NotificationCompat.Builder,
    private val progressBuilder: NotificationCompat.Builder,
    private val resourceProvider: ResourceProvider,
    private val alarmController: AlarmController,
    private val extendHandler: ExtendHandler
) {
    var notification: Notification? = null
    private var alarmViewData: AlarmViewData? = null

    var extending: Boolean = false
        private set
    fun startOver() {
        alarmController.startOver()
//        Log.d("","startOver")
    }

    fun extends() {
        extending = true
        invokeHandlers(extending)
    }
    fun collapse() {
        extending = false
        invokeHandlers(extending)
    }

    private fun invokeHandlers(extending: Boolean) {
        if (extending) {
            extendHandler.onExtend()
        } else {
            extendHandler.onMinimize()
        }
    }

    interface ExtendHandler {
        fun onExtend()
        fun onMinimize()
    }

    fun updateProgress() {
        updateBigView(false)
        notification = progressBuilder
            .setCustomContentView(contentBigView)
            .setCustomBigContentView(contentBigView)
            .build()
    }

    fun createNotificationForFirstTime() {
        updateBigView(false)
        notification = alarmBuilder
            .setCustomContentView(contentSmallView)
            .setCustomBigContentView(contentSmallView)
            .build()
    }

    fun createNotificationForDropDown() {
        extending = true
        updateBigView(true)
        notification = alarmBuilder
            .setCustomContentView(contentBigView)
            .setCustomBigContentView(contentBigView)
            .build()
    }

    private fun updateBigView(alarming: Boolean) {
        if (alarming) {
            contentBigView.setTextViewText(
                R.id.text_current_screen_time,
                resourceProvider.alarmGoesOff
            )
            contentBigView.setProgressBar(R.id.progress_bar, 100, 100, false)
            contentBigView.setProgressBar(R.id.progress_bar_red, 100, 100, false)
            contentBigView.setViewVisibility(R.id.button_collapse,View.GONE)
            setProgressBarToRed(true)
            return
        }
        contentBigView.setViewVisibility(R.id.button_collapse,View.VISIBLE)

        if (alarmController.alarmData == null) return

        if (alarmViewData == null) {
            alarmViewData = AlarmViewData(alarmController.alarmData!!)
        }
        val t = alarmViewData!!.getProgressedTimeFormatted()
        contentBigView.setTextViewText(R.id.text_current_screen_time, t)
        val max =
            AlarmViewData.formatTime(alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMilliSeconds)
        contentBigView.setTextViewText(R.id.text_max_screen_time, max)

        val remaining = alarmViewData!!.remainingMilliSeconds
        val total = alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMilliSeconds
        val progress = 1f - remaining.toFloat() / total.toFloat()
        val p = (progress * 100f).toInt()

        val red = p > 85
        setProgressBarToRed(red)
        contentBigView.setProgressBar(getProgressBarId(red), 100, p, false)
    }

    private fun getProgressBarId(red: Boolean): Int {
        return if (red) {
            R.id.progress_bar_red
        } else {
            R.id.progress_bar
        }
    }

    private fun setProgressBarToRed(red: Boolean) {
        if (red) {
            contentBigView.setViewVisibility(R.id.progress_bar_red, View.VISIBLE)
        } else {
            contentBigView.setViewVisibility(R.id.progress_bar_red, View.GONE)
        }
    }

    class ResourceProvider(
        val alarmGoesOff: String,
        val progressGreenColor: Int,
        val progressRedColor: Int
    )

}
