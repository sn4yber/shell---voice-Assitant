package com.shell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.shell.app.presentation.ShellAppRoot

class MainActivity : ComponentActivity() {
    private val viewModel: ShellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShellAppRoot(viewModel)
        }
    }
}
