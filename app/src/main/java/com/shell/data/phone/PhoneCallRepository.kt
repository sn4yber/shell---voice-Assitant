package com.shell.app.data.phone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

class PhoneCallRepository(
    private val context: Context
) {
    fun startCall(phoneNumber: String) {
        val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
        val callIntent = if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$cleanNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else {
            Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(callIntent)
    }
}
