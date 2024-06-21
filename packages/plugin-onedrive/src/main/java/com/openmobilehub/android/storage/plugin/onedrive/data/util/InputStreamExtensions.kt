package com.openmobilehub.android.storage.plugin.onedrive.data.util

import com.openmobilehub.android.storage.plugin.onedrive.OneDriveConstants
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.toByteArrayOutputStream(): ByteArrayOutputStream {
    val outputStream = ByteArrayOutputStream()

    BufferedInputStream(this).use { input ->
        BufferedOutputStream(outputStream).use { output ->
            val buffer = ByteArray(OneDriveConstants.BUFFER_SIZE)
            var bytesRead: Int

            while (input.read(buffer).also { bytesRead = it } != -1) {
                output.write(buffer, 0, bytesRead)
            }
        }
    }

    return outputStream
}
