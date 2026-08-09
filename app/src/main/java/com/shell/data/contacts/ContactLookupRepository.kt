package com.shell.app.data.contacts

import android.content.ContentResolver
import android.provider.ContactsContract
import java.text.Normalizer

data class ContactMatch(
    val name: String,
    val phoneNumber: String
)

class ContactLookupRepository(
    private val contentResolver: ContentResolver
) {
    fun findFirstMatch(query: String): ContactMatch? {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isBlank()) return null

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex).orEmpty()
                val number = cursor.getString(numberIndex).orEmpty()

                val matchesName = normalize(name).contains(normalizedQuery)
                val matchesNumber = digitsOnly(number).contains(digitsOnly(normalizedQuery))

                if (matchesName || matchesNumber) {
                    return ContactMatch(name = name, phoneNumber = number)
                }
            }
        }

        return null
    }

    private fun normalize(value: String): String {
        return Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .trim()
    }

    private fun digitsOnly(value: String): String {
        return value.filter(Char::isDigit)
    }
}
