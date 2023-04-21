package com.sieunguoimay.screentimealarm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sieunguoimay.screentimealarm.data.AlarmDataController
import com.sieunguoimay.screentimealarm.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var serviceController: ForegroundServiceController
    private lateinit var uiController: UIController
    private lateinit var dataController: AlarmDataController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataController = AlarmDataController()
        serviceController = ForegroundServiceController(applicationContext, dataController)
        uiController = UIController(this, binding, dataController, serviceController)
        uiController.setupEvents()
        tryRequestPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceController.onActivityDestroy()
        dataController.savePersistentData(this)
    }

    override fun onStop() {
        super.onStop()
        dataController.savePersistentData(this)
    }

    private fun tryRequestPermissions() {

        makeSurePermission(Manifest.permission.RECEIVE_BOOT_COMPLETED)
        makeSurePermission(Manifest.permission.VIBRATE)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.FOREGROUND_SERVICE), 1
                )
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 2
                )
            } else {
                onPermissionDenied()
            }
        } else {
            onPermissionGranted()
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
                requestNotificationPermission()
            } else {
                onPermissionDenied()
            }
        } else if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    private fun makeSurePermission(permission: String) {
        if (ContextCompat.checkSelfPermission(
                this, permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission), 3
            )
        }
    }

    private fun onPermissionGranted() {
        serviceController.tryBindingToTheService()
    }

    private fun onPermissionDenied() {
        Toast.makeText(applicationContext, "onPermissionDenied", Toast.LENGTH_SHORT).show()
    }
}