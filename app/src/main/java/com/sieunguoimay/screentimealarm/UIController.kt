package com.sieunguoimay.screentimealarm

import android.app.Activity
import android.util.Log
import android.view.View
import com.sieunguoimay.screentimealarm.data.*
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding
import java.util.*

class UIController(
    private val context: Activity,
    private var binding: ActivityMainBinding,
    private val dataController: AlarmDataController,
    private val serviceController: ForegroundServiceController
) {
    private var timer: Timer = Timer()

    fun setupEvents() {
        Log.d("", "UIController.setupEvents")

        binding.mainButton.setOnClickListener {
            Log.d("", "setOnClickListener")
            serviceController.tryToggleService()
        }
        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = 60
        binding.numberPicker.setOnValueChangedListener { _, _, newValue ->
            Log.d("", "setOnValueChangedListener $newValue")
            dataController.alarmViewData?.alarmData?.alarmConfigData?.setMaxScreenTime(newValue)
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
        binding.mainButton.text = context.getString(key)
        binding.layoutScreenTimeProgress.visibility = if (serviceActive) View.VISIBLE else View.GONE
    }

    private fun updateNumberPicker() {
        binding.numberPicker.value =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMinutes ?: 1
    }

    private fun updateTimeDisplayTexts() {
        val max =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMilliSeconds
        if (max != null) {
            val t = ViewData.formatTime(max)
            binding.textMaxScreenTime.text = t
            binding.textCoolDownTime.text = t
        }
    }

    private fun updateTextCoolDownTime() {

        val remainingText = dataController.alarmViewData?.getProgressedTimeFormatted()
        binding.textCoolDownTime.text = remainingText

        val remaining = dataController.alarmViewData?.remainingMilliSeconds ?: 1
        val total =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTimeMilliSeconds ?: 1
        val progress = 1f - remaining.toFloat() / total.toFloat()
        binding.progressBar.progress = (progress * 100f).toInt()
    }
}