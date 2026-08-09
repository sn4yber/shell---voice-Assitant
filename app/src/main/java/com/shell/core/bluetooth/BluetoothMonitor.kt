package com.shell.app.core.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class BluetoothConnectionState(
    val isBluetoothOn: Boolean = false,
    val isConnected: Boolean = false,
    val connectedLabel: String = "Sin conexión"
)

class BluetoothMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var isRegistered = false

    var state by mutableStateOf(
        BluetoothConnectionState(isBluetoothOn = adapter?.isEnabled == true)
    )
        private set

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val newState = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR
                    )
                    state = state.copy(
                        isBluetoothOn = newState == BluetoothAdapter.STATE_ON,
                        isConnected = if (newState == BluetoothAdapter.STATE_OFF) false else state.isConnected
                    )
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    state = state.copy(
                        isBluetoothOn = true,
                        isConnected = true,
                        connectedLabel = "Bluetooth conectado"
                    )
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    state = state.copy(
                        isConnected = false,
                        connectedLabel = "Sin conexión"
                    )
                }
            }
        }
    }

    fun start() {
        if (isRegistered) return

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        appContext.registerReceiver(receiver, filter)
        isRegistered = true
    }

    fun stop() {
        if (!isRegistered) return
        appContext.unregisterReceiver(receiver)
        isRegistered = false
    }

    fun refresh() {
        state = state.copy(
            isBluetoothOn = adapter?.isEnabled == true
        )
    }
}
