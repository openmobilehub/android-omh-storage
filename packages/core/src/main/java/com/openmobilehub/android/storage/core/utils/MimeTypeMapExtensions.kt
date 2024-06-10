package com.openmobilehub.android.storage.core.utils

import android.webkit.MimeTypeMap

fun MimeTypeMap.getMimeTypeFromUrl(url: String): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    return getMimeTypeFromExtension(extension)
}
