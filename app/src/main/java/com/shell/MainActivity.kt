package com.shell.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ShellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShellApp(viewModel)
        }
    }
}

@Composable
fun ShellApp(viewModel: ShellViewModel) {
    MaterialTheme {
        Surface {
            Text(text = "SHELL — Motorcycle Voice Assistant")
        }
    }
}
