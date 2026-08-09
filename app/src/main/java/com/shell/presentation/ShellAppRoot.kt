package com.shell.app.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.shell.app.ShellViewModel
import com.shell.app.core.permissions.PermissionPreferences
import com.shell.app.core.permissions.PermissionRequirements
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import com.shell.app.core.permissions.ShellPreferencesState
import com.shell.app.domain.music.MusicPlatform

private enum class ShellScreen {
    Onboarding,
    Dashboard,
    Settings
}

@Composable
fun ShellAppRoot(viewModel: ShellViewModel) {
    val context = LocalContext.current
    val preferences = remember(context) { PermissionPreferences(context) }
    val preferencesState by preferences.state.collectAsState(initial = ShellPreferencesState())
    val scope = rememberCoroutineScope()
    var isReady by remember { mutableStateOf(false) }
    var screen by remember {
        mutableStateOf(
            if (preferencesState.onboardingCompleted) ShellScreen.Dashboard else ShellScreen.Onboarding
        )
    }

    fun refresh() {
        isReady = hasAllRuntimePermissions(context) && hasSpecialAccess(context)
    }

    LaunchedEffect(Unit) {
        refresh()
        preferences.syncNotificationKeywordsToMemory()
    }

    LaunchedEffect(preferencesState.onboardingCompleted) {
        if (preferencesState.onboardingCompleted && screen == ShellScreen.Onboarding) {
            screen = ShellScreen.Dashboard
        }
    }

    LaunchedEffect(preferencesState.importantNotificationPackages, preferencesState.quietNotificationKeywords) {
        com.shell.app.data.notifications.NotificationCenter.updateQuietKeywords(preferencesState.quietNotificationKeywords)
    }

    LaunchedEffect(preferencesState.musicPlatform) {
        viewModel.setMusicPlatform(preferencesState.musicPlatform)
    }

    if (!preferencesState.onboardingCompleted || screen == ShellScreen.Onboarding) {
        PermissionOnboardingScreen(
            onRequestRefresh = {
                refresh()
            },
            onAccept = {
                scope.launch {
                    preferences.setOnboardingCompleted(true)
                }
                screen = ShellScreen.Dashboard
            },
            onDeny = {
                scope.launch {
                    preferences.setOnboardingCompleted(true)
                }
                screen = ShellScreen.Dashboard
            }
        )
    } else if (screen == ShellScreen.Settings) {
        ShellSettingsScreen(
            preferences = preferences,
            preferencesState = preferencesState,
            onBack = { screen = ShellScreen.Dashboard }
        )
    } else {
        ShellDashboardScreen(
            viewModel = viewModel,
            permissionsReady = isReady,
            autoListenEnabled = preferencesState.autoListen,
            musicPlatform = preferencesState.musicPlatform,
            onManagePermissions = { screen = ShellScreen.Onboarding },
            onOpenSettings = { screen = ShellScreen.Settings },
            onMusicPlatformChange = { platform ->
                scope.launch {
                    preferences.setMusicPlatform(platform)
                }
            }
        )
    }
}

private fun hasAllRuntimePermissions(context: Context): Boolean {
    return PermissionRequirements.runtimeGroups
        .flatMap { it.permissions }
        .all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
}

private fun hasSpecialAccess(context: Context): Boolean {
    return PermissionRequirements.specialAccesses.all { access -> access.isGranted(context) }
}
