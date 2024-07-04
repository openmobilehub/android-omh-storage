package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.mapper

import android.webkit.MimeTypeMap
import java.io.File

class LocalFileToMimeType(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(file: File): String? {
        return mimeTypeMap.getMimeTypeFromExtension(file.extension)
    }
}
