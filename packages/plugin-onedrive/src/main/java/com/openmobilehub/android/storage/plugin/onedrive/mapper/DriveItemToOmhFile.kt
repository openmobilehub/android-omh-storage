package com.openmobilehub.android.storage.plugin.onedrive.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileType
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces

class DriveItemToOmhFile(private val mimeTypeMap: MimeTypeMap) {
    operator fun invoke(driveItem: DriveItem): OmhFile {
        driveItem.run {
            val isFolder = folder != null
            val mimeType = if (isFolder) {
                OmhFileType.OMH_FOLDER.mimeType
            } else {
                mimeTypeMap.getMimeTypeFromUrl(
                    name.removeWhitespaces()
                )
            }

            return OmhFile(
                mimeType.orEmpty(),
                id,
                name,
                // OffsetDateTime stringifies to the RFC3339 format
                lastModifiedDateTime.toString(),
                parentReference.id.orEmpty()
            )
        }
    }
}
