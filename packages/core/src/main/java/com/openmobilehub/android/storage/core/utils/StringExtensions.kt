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

package com.openmobilehub.android.storage.core.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.removeWhitespaces(): String {
    return this.replace("\\s".toRegex(), "_")
}

fun String.removeSpecialCharacters(): String {
    return this.replace("[^a-zA-Z0-9.]".toRegex(), "_")
}

fun String.fromRFC3339StringToDate(): Date? {
    val rfc3339Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    rfc3339Format.timeZone = TimeZone.getTimeZone("UTC")

    return try {
        rfc3339Format.parse(this)
    } catch (e: ParseException) {
        null
    }
}

fun String.splitPathToParts(): List<String> {
    return if (!contains('/')) {
        listOf(this)
    } else {
        if (endsWith("/")) {
            substringBeforeLast("/").split("/")
        } else {
            split("/")
        }.filter { it.isNotBlank() && it.isNotEmpty() }
    }
}
