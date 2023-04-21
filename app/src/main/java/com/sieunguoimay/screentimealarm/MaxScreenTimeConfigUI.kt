package com.sieunguoimay.screentimealarm

import android.R
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.sieunguoimay.screentimealarm.data.AlarmConfigData
import com.sieunguoimay.screentimealarm.data.AlarmViewData


class MaxScreenTimeConfigUI(
    private val context: Context,
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
        numberPicker.setOnFocusChangeListener { _, focused ->
            if (!focused) {
                showingNumberPicker = false
                updateNumberPickerVisibility(false)
            }
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
        val color = fetchColor(R.attr.colorBackground)
        val targetColor = Color.parseColor("#c0925c")
        val toggledColor = ColorUtils.blendARGB(color, targetColor, 0.25f)
        layout.setBackgroundColor(if (showing) toggledColor else color)
        if (showing) {
            numberPicker.requestFocus()
        } else {
            numberPicker.clearFocus()
        }
    }

    private fun fetchColor(attr: Int): Int {
        val typedValue = TypedValue()
        val a: TypedArray =
            context.obtainStyledAttributes(typedValue.data, intArrayOf(attr))
        val color = a.getColor(0, 0)
        a.recycle()
        return color
    }
}