package com.openmobilehub.android.storage.plugin.googledrive.gms.data.extension

import com.google.api.services.drive.model.File
import com.openmobilehub.android.storage.plugin.googledrive.gms.GoogleDriveGmsConstants.FOLDER_MIME_TYPE

fun File.isFolder() =
    mimeType == FOLDER_MIME_TYPE
