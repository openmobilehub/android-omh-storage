/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
