package com.sieunguoimay.screentimealarm

import android.content.Context
import android.util.Log
import com.sieunguoimay.screentimealarm.data.*
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding

class UIController(
    private val context: Context,
    private var binding: ActivityMainBinding,
    private val dataController: AlarmDataController,
    private val serviceController: ForegroundServiceController
) {

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
    private fun updateNumberPicker(){
        binding.numberPicker.value =
            dataController.alarmViewData?.alarmData?.alarmConfigData?.maxScreenTime ?: 1
    }
}