package com.shell.app.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService

data class RuntimePermissionGroup(
    val title: String,
    val permissions: List<String>
)

data class SpecialAccess(
    val title: String,
    val isGranted: (Context) -> Boolean,
    val settingsIntent: (Context) -> Intent
)

object PermissionRequirements {
    val runtimeGroups = listOf(
        RuntimePermissionGroup("Voz", listOf(Manifest.permission.RECORD_AUDIO)),
        RuntimePermissionGroup("Contactos", listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)),
        RuntimePermissionGroup("Llamadas", listOf(Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE)),
        RuntimePermissionGroup("Notificaciones", listOf(Manifest.permission.POST_NOTIFICATIONS)),
        RuntimePermissionGroup("Ubicación", listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)),
        RuntimePermissionGroup("Bluetooth", listOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN))
    )

    val specialAccesses = listOf(
        SpecialAccess(
            title = "Notificaciones en segundo plano",
            isGranted = { context ->
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            },
            settingsIntent = { context -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            } }
        ),
        SpecialAccess(
            title = "Notification Listener",
            isGranted = { context ->
                val enabled = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                ).orEmpty()
                enabled.contains(context.packageName)
            },
            settingsIntent = { Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS) }
        ),
        SpecialAccess(
            title = "Accesibilidad",
            isGranted = { context ->
                val enabled = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                ).orEmpty()
                enabled.contains(context.packageName)
            },
            settingsIntent = { Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS) }
        ),
        SpecialAccess(
            title = "No molestar",
            isGranted = { context ->
                context.getSystemService<android.app.NotificationManager>()
                    ?.isNotificationPolicyAccessGranted == true
            },
            settingsIntent = { Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS) }
        ),
        SpecialAccess(
            title = "Optimización de batería",
            isGranted = { context ->
                val pm = context.getSystemService<PowerManager>()
                pm?.isIgnoringBatteryOptimizations(context.packageName) == true
            },
            settingsIntent = { context -> Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            } }
        )
    )
}

