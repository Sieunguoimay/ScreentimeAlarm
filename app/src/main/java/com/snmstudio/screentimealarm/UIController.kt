package com.snmstudio.screentimealarm

import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.ProgressBar
import android.widget.TextView
import com.sieunguoimay.screentimealarm.R
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding
import com.snmstudio.screentimealarm.data.*
import java.util.*

class UIController(
    private val context: Activity,
    private var binding: ActivityMainBinding,
    private val dataController: AlarmDataController,
    private val serviceController: ForegroundServiceController,
    private val startOverButtonClickHandler: ProgressRunningUI.StartOverButtonClickHandler
) {
    private val mainButton: Button get() = binding.mainButton
    private val numberPicker: NumberPicker get() = binding.numberPicker
    private val startOverButton: Button get() = binding.buttonStartOver
    private val progressLayout: LinearLayout get() = binding.layoutScreenTimeProgress
    private val statusText: TextView get() = binding.textLine2Value
    private val bottomGuideText: TextView get() = binding.textBottomGuide
    private val screenTimeMaxConfigText: TextView get() = binding.textLine1Value
    private val progressBar: ProgressBar get() = binding.progressBar
    private val currentScreenTimeText: TextView get() = binding.textCurrentScreenTime
    private val maxScreenTimeText: TextView get() = binding.textMaxScreenTime

    private var progressRunningUI: ProgressRunningUI? = null
    private var maxScreenTimeConfigUI: MaxScreenTimeConfigUI? = null
    fun setupEvents() {

        mainButton.setOnClickListener {
            if (progressRunningUI != null) {
                if (!progressRunningUI!!.mainButtonLock) {
                    progressRunningUI!!.mainButtonLock = true
                    serviceController.tryToggleService()
                }
            }
        }
        numberPicker.minValue = 1
        numberPicker.maxValue = 60
        numberPicker.setOnValueChangedListener { _, _, newValue ->
            Log.d("", "setOnValueChangedListener $newValue")
            dataController.alarmViewData?.alarmData?.alarmConfigData?.setMaxScreenTime(newValue)
        }

        dataController.addHandler(dataHandler)
        serviceController.addHandler(serviceActiveHandler)

        progressRunningUI = ProgressRunningUI(
            context,
            startOverButton,
            progressBar,
            maxScreenTimeText,
            currentScreenTimeText,
            startOverButtonClickHandler
        )
        maxScreenTimeConfigUI = MaxScreenTimeConfigUI(
            context,
            binding.layoutMaxScreenTimeConfig,
            binding.textLine1Value,
            binding.imageEditIcon,
            binding.numberPicker
        )
        maxScreenTimeConfigUI!!.activate()
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
            progressRunningUI?.mainButtonLock = false

            dataController.alarmViewData?.alarmData?.alarmRuntimeData?.setAlarmActiveStatus(serviceController.isActive)

            if (serviceController.isActive) {
                if (dataController.alarmViewData != null) {
                    progressRunningUI?.setup(dataController.alarmViewData!!)
                }
                maxScreenTimeConfigUI?.inactivate()
            } else {
                progressRunningUI?.tearDown()
                maxScreenTimeConfigUI?.activate()
            }
        }
    }

    private fun syncViewWithData() {
        toggleUIWithServiceActiveness()
        updateNumberPicker()
        updateTimeDisplayTexts()
        maxScreenTimeConfigUI!!.setData(dataController.alarmViewData?.alarmData?.alarmConfigData!!)
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
        bottomGuideText.visibility = if(serviceActive) View.VISIBLE else View.GONE
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
            val t = AlarmViewData.formatTime(max)
            val zero = AlarmViewData.formatTime(0)
            maxScreenTimeText.text = t
            currentScreenTimeText.text = zero
            screenTimeMaxConfigText.text = t
        }
    }
}