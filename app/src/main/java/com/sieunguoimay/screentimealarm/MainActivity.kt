package com.sieunguoimay.screentimealarm

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var serviceController: ServiceController
    private lateinit var uiController: UIController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serviceController = ServiceController(applicationContext, serviceActiveHandler)
        uiController = UIController(binding, serviceController)

        uiController.setupEvents()

        serviceController.tryBindingToTheService()

        tryRequestPermissions()

    }

    private fun tryRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            onPermissionGranted()
        } else {
            // Permission is not granted, request it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.FOREGROUND_SERVICE), 1
                )
            } else {
                onPermissionDenied()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun onPermissionGranted() {
        uiController.toggleMainButton(true)
    }

    private fun onPermissionDenied() {
        Toast.makeText(applicationContext, "onPermissionDenied", Toast.LENGTH_SHORT).show()
    }

    private val serviceActiveHandler = object : ServiceController.ServiceActiveHandler {
        override fun onServiceActiveChanged(sender: ServiceController) {
            uiController.toggleMainButtonText()
        }
    }
}