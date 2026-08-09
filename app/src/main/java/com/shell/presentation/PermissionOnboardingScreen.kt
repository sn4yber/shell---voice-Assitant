package com.shell.app.presentation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shell.app.core.permissions.PermissionRequirements

@Composable
fun PermissionOnboardingScreen(
    onRequestRefresh: () -> Unit,
    onAccept: () -> Unit,
    onDeny: () -> Unit
) {
    val context = LocalContext.current
    val runtimeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onRequestRefresh()
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "Shell necesita permisos")
                Text(text = "Para controlar el teléfono por voz desde el intercomunicador.")

                PermissionRequirements.runtimeGroups.forEach { group ->
                    Text(text = "• ${group.title}")
                }
                PermissionRequirements.specialAccesses.forEach { access ->
                    Text(text = "• ${access.title}")
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    runtimeLauncher.launch(
                        PermissionRequirements.runtimeGroups.flatMap { it.permissions }.toTypedArray()
                    )
                }) {
                    Text(text = "Conceder permisos")
                }

                PermissionRequirements.specialAccesses.forEach { access ->
                    Button(onClick = {
                        context.startActivity(access.settingsIntent(context))
                    }) {
                        Text(text = "Abrir ${access.title}")
                    }
                }

                Button(onClick = onAccept) {
                    Text(text = "Aceptar y continuar")
                }
                Button(onClick = onDeny) {
                    Text(text = "Denegar por ahora")
                }
            }
        }
    }
}
