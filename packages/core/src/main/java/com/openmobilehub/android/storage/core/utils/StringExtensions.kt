package com.openmobilehub.android.storage.core.utils

fun String.removeWhitespaces(): String {
    return this.replace("\\s".toRegex(), "_")
}
