package com.ogapirlo.takken

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.ogapirlo.takken.notify.ReminderScheduler
import com.ogapirlo.takken.ui.AppViewModel
import com.ogapirlo.takken.ui.TakkenApp
import com.ogapirlo.takken.ui.theme.TakkenTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderScheduler.ensureChannel(this)
        ReminderScheduler.apply(this)
        setContent {
            TakkenTheme {
                TakkenApp(vm, requestNotificationPermission = ::requestNotificationPermission)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        vm.refreshDay()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
