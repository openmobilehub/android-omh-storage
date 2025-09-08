package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

sealed interface NodeMetadata {
    val id: String
    val name: String
    val path: String

    companion object {
        const val ATTR_TAG = ".tag"
        const val TAG_FILE = "file"
        const val TAG_FOLDER = "folder"
    }
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FolderMetadata(
    @JsonProperty(".tag")
    val tag: String? = "folder",
    @JsonProperty("id")
    override val id: String,
    @JsonProperty("name")
    override val name: String,
    @JsonProperty("path_display")
    override val path: String,
    @JsonProperty("sharing_info")
    val sharingInfo: FolderSharingInfo? = null,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String? = null
) : NodeMetadata
// {
//    "metadata": {
//        "id": "id:<REMOVED>",
//        "name": "math",
//        "path_display": "/Homework/math",
//        "path_lower": "/homework/math",
//        "property_groups": [
//            {
//                "fields": [
//                    {
//                        "name": "Security Policy",
//                        "value": "Confidential"
//                    }
//                ],
//                "template_id": "ptid:<REMOVED>"
//            }
//        ],
//        "sharing_info": {
//            "no_access": false,
//            "parent_shared_folder_id": "<REMOVED>",
//            "read_only": false,
//            "traverse_only": false
//        }
//    }
// }

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class FileMetadata(
    @JsonProperty(".tag")
    val tag: String? = "file",
    @JsonProperty("content_hash")
    val contentHash: String? = null,
    @JsonProperty("has_explicit_shared_members")
    val hasExplicitSharedMembers: Boolean,
    @JsonProperty("id")
    override val id: String,
    @JsonProperty("is_downloadable")
    val downloadAble: Boolean,
    @JsonProperty("name")
    override val name: String,
    @JsonProperty("path_display")
    override val path: String,
    @JsonProperty("size")
    val size: Long,
    @JsonProperty("client_modified")
    val clientModified: String? = null,
    @JsonProperty("server_modified")
    val serverModified: String? = null,
    @JsonProperty("sharing_info")
    val sharingInfo: FileSharingInfo? = null,
    @JsonProperty("rev")
    val rev: String? = null,
    @JsonProperty("export_info")
    val exportInfo: ExportInfo? = null,
) : NodeMetadata

// "metadata": {
//    ".tag": "file",
//    "client_modified": "2015-05-12T15:50:38Z",
//    "content_hash": "<REMOVED>",
//    "file_lock_info": {
//        "created": "2015-05-12T15:50:38Z",
//        "is_lockholder": true,
//        "lockholder_name": "Imaginary User"
//    },
//    "has_explicit_shared_members": false,
//    "id": "id:<REMOVED>",
//    "is_downloadable": true,
//    "name": "Prime_Numbers.txt",
//    "path_display": "/Homework/math/Prime_Numbers.txt",
//    "path_lower": "/homework/math/prime_numbers.txt",
//    "property_groups": [
//    {
//        "fields": [
//        {
//            "name": "Security Policy",
//            "value": "Confidential"
//        }
//        ],
//        "template_id": "ptid:<REMOVED>"
//    }
//    ],
//    "rev": "a1c10ce0dd78",
//    "server_modified": "2015-05-12T15:50:38Z",
//    "sharing_info": {
//        "modified_by": "dbid:<REMOVED>",
//        "parent_shared_folder_id": "84528192421",
//        "read_only": true
//    },
//    "size": 7212
// }
