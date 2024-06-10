package com.openmobilehub.android.storage.core.utils

import android.webkit.MimeTypeMap

object FileUtils {
    fun getExtension(string: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(string)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
}
