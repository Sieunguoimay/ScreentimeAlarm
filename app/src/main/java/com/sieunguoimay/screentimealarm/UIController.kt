package com.sieunguoimay.screentimealarm

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import com.sieunguoimay.screentimealarm.data.*
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.schedule

class UIController(
    private val context: Activity,
    private var binding: ActivityMainBinding,
    private val dataController: AlarmDataController,
    private val serviceController: ForegroundServiceController
) {
    private var timer: Timer = Timer()

    private val mainButton: Button get() = binding.mainButton
    private val numberPicker: NumberPicker get() = binding.numberPicker
    private val startOverButton: Button get() = binding.buttonStartOver
    private val progressLayout: LinearLayout get() = binding.layoutScreenTimeProgress
    private val statusText: TextView get() = binding.textLine2Value
    private val screenTimeMaxConfigText: TextView get() = binding.textLine1Value
    private val progressBar: ProgressBar get() = binding.progressBar
    private val currentScreenTimeText: TextView get() = binding.textCurrentScreenTime
    private val maxScreenTimeText: TextView get() = binding.textMaxScreenTime

    private var mainButtonLock:Boolean = false
    fun setupEvents() {

        mainButton.setOnClickListener {
            if(!mainButtonLock){
                mainButtonLock = true
                serviceController.tryToggleService()
            }
        }
        numberPicker.minValue = 1
        numberPicker.maxValue = 60
        numberPicker.setOnValueChangedListener { _, _, newValue ->
            Log.d("", "setOnValueChangedListener $newValue")
            dataController.alarmViewData?.alarmData?.alarmConfigData?.setMaxScreenTime(newValue)
        }
        startOverButton.setOnClickListener {
            if(!mainButtonLock){
                dataController.alarmViewData?.alarmController?.startOver()
            }
        }

        dataController.addHandler(dataHandler)
        serviceController.addHandler(serviceActiveHandler)
    }

    private val dataHandler = object : AlarmDataHandler {
        override fun onDataReady(sender: AlarmDataController) {
            Log.d("", "onDataReady")
            setupViewData()
            syncViewWithData()
        }
    }


    private fun setupViewData() {
        dataController.alarmViewData?.alarmData?.alarmRuntimeData?.changeHandler?.add(
            runtimeDataHandler
        )
    }

    private val runtimeDataHandler = object : AlarmRuntimeDataChangeHandler {
        override fun onChange(data: AlarmRuntimeData) {
            syncViewWithData()
            Log.d("", "runtimeDataHandler.onChange")
        }
    }
    private val serviceActiveHandler = object : ForegroundServiceController.ServiceActiveHandler {
        override fun onConnectionStatusChanged(sender: ForegroundServiceController) {
            mainButtonLock = false
            if (serviceController.isActive) {
                runTimerTask()
            } else {
                stopTimerTask()
            }
        }
    }

    private fun runTimerTask() {
        val interval = 500 // interval in milliseconds
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // This code will run every 0.5 seconds
                context.runOnUiThread {
                    updateTextCoolDownTime()
                }
            }
        }, 0, interval.toLong())
    }

    private fun stopTimerTask() {
        timer.cancel()
    }

    private fun syncViewWithData() {
        toggleUIWithServiceActiveness()
        updateNumberPicker()
        updateTimeDisplayTexts()
    }

    private fun toggleUIWithServiceActiveness() {
        Log.d("", "toggleMainButtonText")
        val serviceActive =
            dataController.alarmViewData?.alarmData?.alarmRuntimeData?.alarmActiveStatus == true
        val key = if (serviceActive) R.string.disable else R.string.enable
        val statusKey = if (serviceActive) R.string.on else R.string.off
        toggleVisibility(serviceActive)
        mainButton.text = context.getString(key)
        statusText.text = context.getString(statusKey)
    }

    private fun toggleVisibility(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        progressLayout.visibility = visibility
        startOverButton.visibility = visibility
    }

    private fun updateNumberPicker() {
        numberPicker.value =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMinutes ?: 1
    }

    private fun updateTimeDisplayTexts() {
        val max =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMilliSeconds
        if (max != null) {
            val t = ViewData.formatTime(max)
            val zero = ViewData.formatTime(0)
            maxScreenTimeText.text = t
            currentScreenTimeText.text = zero
            screenTimeMaxConfigText.text = t
        }
    }

    private fun updateTextCoolDownTime() {

        val t = dataController.alarmViewData?.getProgressedTimeFormatted()
        currentScreenTimeText.text = t

        val remaining = dataController.alarmViewData?.remainingMilliSeconds ?: 1
        val total =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMilliSeconds ?: 1
        val progress = 1f - remaining.toFloat() / total.toFloat()
        progressBar.progress = (progress * 100f).toInt()
    }
}