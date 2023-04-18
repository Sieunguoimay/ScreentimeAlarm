package com.sieunguoimay.screentimealarm

import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.sieunguoimay.screentimealarm.data.AlarmViewData
import java.util.*

class ProgressRunningUI(
    private val startOverButton: Button,
    private val progressBar: ProgressBar,
    private val maxScreenTimeText: TextView,
    private val currentScreenTimeText: TextView,
) {
    private var mainButtonLock: Boolean = false
    private var timer: Timer = Timer()
    private val mHandler = Handler(Looper.getMainLooper())
    private var alarmViewData: AlarmViewData? = null

    fun setup(alarmViewData: AlarmViewData) {
        this.alarmViewData = alarmViewData
        startOverButton.setOnClickListener {
            if (!mainButtonLock) {
                alarmViewData.alarmController?.startOver()
            }
        }
        syncViewWithData()
        runTimerTask()
    }

    fun tearDown() {
        stopTimerTask()
    }

    private fun syncViewWithData() {
        updateTimeDisplayTexts()
    }

    private fun updateTimeDisplayTexts() {
        if (alarmViewData == null) return
        val max = alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMilliSeconds
        maxScreenTimeText.text = AlarmViewData.formatTime(max)
        currentScreenTimeText.text = AlarmViewData.formatTime(0)
    }

    private fun runTimerTask() {
        val interval = 500 // interval in milliseconds
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mHandler.post {
                    updateCoolDownTimeProgress()
                }
            }
        }, 0, interval.toLong())
    }

    private fun stopTimerTask() {
        timer.cancel()
    }

    private fun updateCoolDownTimeProgress() {
        if (alarmViewData == null) return

        val t = alarmViewData!!.getProgressedTimeFormatted()
        currentScreenTimeText.text = t

        val remaining = alarmViewData!!.remainingMilliSeconds
        val total = alarmViewData!!.alarmData.alarmConfigData.maxScreenTimeMilliSeconds
        val progress = 1f - remaining.toFloat() / total.toFloat()
        progressBar.progress = (progress * 100f).toInt()
    }
}