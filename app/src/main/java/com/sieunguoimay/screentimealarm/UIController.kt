package com.sieunguoimay.screentimealarm

import android.content.Context
import android.util.Log
import com.sieunguoimay.screentimealarm.data.*
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding
import java.util.*

class UIController(
    private val context: Context,
    private var binding: ActivityMainBinding,
    private val dataController: AlarmDataController,
    private val serviceController: ForegroundServiceController
) {
    private var timer:Timer = Timer()

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
    private val serviceActiveHandler = object : ForegroundServiceController.ServiceActiveHandler {
        override fun onConnectionStatusChanged(sender: ForegroundServiceController) {
            if(serviceController.isActive){
                runTimerTask()
            }else{
                stopTimerTask()
            }
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

    private fun runTimerTask() {
        val interval = 500 // interval in milliseconds
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // This code will run every 0.5 seconds
                updateTextCoolDownTime()
            }
        }, 0, interval.toLong())
    }
    private fun stopTimerTask(){
        timer.cancel()
    }

    private fun syncViewWithData() {
        toggleMainButtonText()
        updateNumberPicker()
    }

    private fun toggleMainButtonText() {
        Log.d("", "toggleMainButtonText")
        val key =
            if (dataController.alarmViewData?.alarmData?.alarmRuntimeData?.alarmActiveStatus == true) R.string.disable else R.string.enable
        binding.mainButton.text = context.getString(key)
    }

    private fun updateNumberPicker() {
        binding.numberPicker.value =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTime ?: 1
    }

    private fun updateTextCoolDownTime() {
        val remaining = dataController.alarmViewData?.remainingTimeFormatted
        binding.textCoolDownTime.text = remaining
    }
}