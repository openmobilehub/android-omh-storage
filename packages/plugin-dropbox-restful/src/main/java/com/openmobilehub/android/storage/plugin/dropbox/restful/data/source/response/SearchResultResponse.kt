package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFolder

@Keep
data class SearchResultResponse(
    val matches: List<NodeMetadata>
) {
    fun toListOfOmhStorageEntity(): List<OmhStorageEntity> {
        return matches.map {
            when (it) {
                is FileMetadata -> it.toOmhFile("")
                is FolderMetadata -> it.toOmhFolder("")
            }
        }
    }
}
