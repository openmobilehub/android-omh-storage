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

@Suppress("MagicNumber")
fun String.escapeUnicode(): String =
    this.map {
        if (it.code <= 127 || it == '/' || it == '.') {
            it.toString()
        } else {
            "\\u%04x".format(it.code)
        }
    }.joinToString("")

@Suppress("MagicNumber")
fun String.unescapeUnicode(): String {
    val regex = Regex("""\\u([0-9a-fA-F]{4})""")
    return regex.replace(this) { matchResult ->
        val hexValue = matchResult.groupValues[1]
        val intValue = hexValue.toInt(16)
        intValue.toChar().toString()
    }
}

@Suppress("ReturnCount", "MagicNumber")
fun String.fromRFC3339StringToDate(): Date? {
    if (this.isEmpty()) return null

    // Use regex to check if the given string has fractional seconds
    val processed = this.replace(Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})\\.(\\d+)(Z)$")) { mr ->
        val prefix = mr.groupValues[1]
        val fraction = mr.groupValues[2]
        val suffix = mr.groupValues[3]
        val truncated = if (fraction.length > 3) fraction.substring(0, 3) else fraction.padEnd(3, '0')
        "$prefix.$truncated$suffix"
    }
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", // with fractional (normalized to 3)
        "yyyy-MM-dd'T'HH:mm:ss'Z'" // without fractional
    )
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(processed)
        } catch (_: ParseException) {
            // continue
        }
    }
    return null
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
