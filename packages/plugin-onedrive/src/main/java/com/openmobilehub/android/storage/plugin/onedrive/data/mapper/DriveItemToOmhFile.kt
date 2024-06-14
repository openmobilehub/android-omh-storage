package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import android.webkit.MimeTypeMap
import com.microsoft.graph.models.DriveItem
import com.openmobilehub.android.storage.core.model.OmhFile
import com.openmobilehub.android.storage.core.model.OmhFileType
import com.openmobilehub.android.storage.core.utils.getMimeTypeFromUrl
import com.openmobilehub.android.storage.core.utils.removeWhitespaces
import com.openmobilehub.android.storage.core.utils.toRFC3339String
import java.util.Date

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

            val instant = lastModifiedDateTime.toInstant()
            val lastModifiedDate = Date.from(instant)

            return OmhFile(
                mimeType.orEmpty(),
                id,
                name,
                lastModifiedDate.toRFC3339String(),
                parentReference.id.orEmpty()
            )
        }
    }
}
