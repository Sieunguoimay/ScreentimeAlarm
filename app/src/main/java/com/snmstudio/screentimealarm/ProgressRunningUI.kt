package com.snmstudio.screentimealarm

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.sieunguoimay.screentimealarm.R
import com.snmstudio.screentimealarm.data.AlarmViewData
import java.util.*

class ProgressRunningUI(
    private val context: Context,
    private val startOverButton: Button,
    private val progressBar: ProgressBar,
    private val maxScreenTimeText: TextView,
    private val currentScreenTimeText: TextView,
    private val startOverClickHandler:StartOverButtonClickHandler
) {
    var mainButtonLock: Boolean = false
    private var timer: Timer = Timer()
    private val mHandler = Handler(Looper.getMainLooper())
    private var alarmViewData: AlarmViewData? = null

    fun setup(alarmViewData: AlarmViewData) {
        this.alarmViewData = alarmViewData
        startOverButton.setOnClickListener {
            if (!mainButtonLock) {
                startOverClickHandler.onStartOverClicked()
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
        val greenColor = context.getColor(R.color.progress)
        val redColor = context.getColor(R.color.progress_red)
        progressBar.progressTintList =
            ColorStateList.valueOf(calculateProgressTint(greenColor, redColor, progress))
//        Log.d("","progress ${alarmViewData!!.alarmData.alarmRuntimeData.alarmFireTime} $total")
    }

    companion object {

        fun calculateProgressTint(
            greenColor: Int,
            redColor: Int,
            progress: Float
        ): Int {
            val color =
                ColorUtils.blendARGB(greenColor, redColor, clampProgress(progress, 0.7f, .95f))
            return (color)
        }

        private fun clampProgress(progress: Float, min: Float, max: Float): Float {
            if (progress <= min) return 0f
            if (progress >= max) return 1f
            return (progress - min) / (max - min)
        }
    }
    interface StartOverButtonClickHandler{
        fun onStartOverClicked()
    }
}