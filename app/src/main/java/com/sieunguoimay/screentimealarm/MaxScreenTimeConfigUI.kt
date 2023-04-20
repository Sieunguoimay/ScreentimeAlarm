package com.sieunguoimay.screentimealarm

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.sieunguoimay.screentimealarm.data.AlarmConfigData
import com.sieunguoimay.screentimealarm.data.AlarmViewData


class MaxScreenTimeConfigUI(
    private val layout: LinearLayout,
    private val textView: TextView,
    private val editIcon: ImageView,
    private val numberPicker: NumberPicker
) {

    private var showingNumberPicker: Boolean = false
    private var alarmConfigData: AlarmConfigData? = null
    private var activated: Boolean = false

    fun setData(alarmConfigData: AlarmConfigData) {
        this.alarmConfigData = alarmConfigData
    }

    fun activate() {
        editIcon.visibility = View.VISIBLE
        layout.setOnClickListener {
            if (activated) {
                toggleNumberPicker()
            }
        }
        numberPicker.setOnValueChangedListener { _, _, newValue ->
            this.alarmConfigData?.setMaxScreenTime(newValue)
            updateMaxScreenTimeTextView()
        }
        updateNumberPickerVisibility(showingNumberPicker)
        activated = true
    }

    fun inactivate() {
        updateNumberPickerVisibility(false)
        editIcon.visibility = View.GONE
        activated = false
    }

    private fun updateMaxScreenTimeTextView() {
        if (alarmConfigData == null) return
        val t = AlarmViewData.formatTime(alarmConfigData!!.maxScreenTimeMilliSeconds)
        textView.text = t
    }

    private fun toggleNumberPicker() {
        showingNumberPicker = !showingNumberPicker
        updateNumberPickerVisibility(showingNumberPicker)
    }

    private fun updateNumberPickerVisibility(showing: Boolean) {
        numberPicker.visibility = if (showing) View.VISIBLE else View.GONE

        textView.setBackgroundColor(
            ColorUtils.blendARGB(
                Color.parseColor("#fff"),
                Color.BLACK,
                1f
            )
        )
    }
}