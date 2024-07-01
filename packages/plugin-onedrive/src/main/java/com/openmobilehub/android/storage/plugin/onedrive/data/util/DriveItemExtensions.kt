package com.openmobilehub.android.storage.plugin.onedrive.data.util

import com.microsoft.graph.models.DriveItem
import java.io.StringWriter
import java.nio.charset.Charset

fun DriveItem.serializeToString(): String {
    val writer = StringWriter()

    val properties = listOf(
        "@odata.type" to odataType,
        "additionalData" to additionalData?.toString(),
        "analytics" to analytics?.toString(),
        "audio" to audio?.toString(),
        "bundle" to bundle?.toString(),
        "children" to children?.joinToString { it.toString() },
        "content" to content?.toString(Charset.defaultCharset()),
        "createdBy" to createdBy?.toString(),
        "createdByUser" to createdByUser?.toString(),
        "createdDateTime" to createdDateTime?.toString(),
        "cTag" to cTag,
        "deleted" to deleted?.toString(),
        "description" to description,
        "eTag" to eTag,
        "file" to file?.toString(),
        "fileSystemInfo" to fileSystemInfo?.toString(),
        "folder" to folder?.toString(),
        "id" to id,
        "image" to image?.toString(),
        "lastModifiedBy" to lastModifiedBy?.toString(),
        "lastModifiedByUser" to lastModifiedByUser?.toString(),
        "lastModifiedDateTime" to lastModifiedDateTime?.toString(),
        "listItem" to listItem?.toString(),
        "location" to location?.toString(),
        "malware" to malware?.toString(),
        "name" to name,
        "package" to `package`?.toString(),
        "parentReference" to parentReference?.toString(),
        "pendingOperations" to pendingOperations?.toString(),
        "permissions" to permissions?.joinToString { it.toString() },
        "photo" to photo?.toString(),
        "publication" to publication?.toString(),
        "remoteItem" to remoteItem?.toString(),
        "retentionLabel" to retentionLabel?.toString(),
        "root" to root?.toString(),
        "searchResult" to searchResult?.toString(),
        "shared" to shared?.toString(),
        "sharepointIds" to sharepointIds?.toString(),
        "size" to size?.toString(),
        "specialFolder" to specialFolder?.toString(),
        "subscriptions" to subscriptions?.joinToString { it.toString() },
        "thumbnails" to thumbnails?.joinToString { it.toString() },
        "versions" to versions?.joinToString { it.toString() },
        "video" to video?.toString(),
        "webDavUrl" to webDavUrl,
        "workbook" to workbook?.toString()
    ).sortedBy { it.first }

    properties.forEach { (name, value) ->
        if (value == null) {
            return@forEach
        }

        writer.append("$name: ").append(value).append("\n")
    }

    return writer.toString()
}
