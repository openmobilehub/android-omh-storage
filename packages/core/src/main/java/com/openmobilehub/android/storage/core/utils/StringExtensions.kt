package com.openmobilehub.android.storage.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String.removeWhitespaces(): String {
    return this.replace("\\s".toRegex(), "_")
}

fun String.fromRFC3339StringToDate(): Date? {
    val rfc3339Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    rfc3339Format.timeZone = TimeZone.getTimeZone("UTC")

    return rfc3339Format.parse(this)
}
