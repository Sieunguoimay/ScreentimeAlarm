package com.sieunguoimay.screentimealarm

import android.util.Log
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding

class UIController(
    private var binding: ActivityMainBinding,
    private var serviceController: ForegroundServiceController
) {

    fun setupEvents() {
        binding.mainButton.isEnabled = false
        binding.mainButton.setOnClickListener {
            Log.d("", "setOnClickListener")
            serviceController.tryToggleService()
        }
        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = 60
        binding.numberPicker.setOnValueChangedListener { _, _, newValue ->
            Log.d("", "setOnValueChangedListener $newValue")
            serviceController.setAlarmTime(newValue)
        }
        toggleMainButtonText()
    }

    fun toggleMainButton(active: Boolean) {
        binding.mainButton.isEnabled = active
    }

    fun toggleMainButtonText() {
        var key = if (serviceController.getActive()) R.string.disable else R.string.enable
        binding.mainButton.text = serviceController.context.getString(key)
    }
}