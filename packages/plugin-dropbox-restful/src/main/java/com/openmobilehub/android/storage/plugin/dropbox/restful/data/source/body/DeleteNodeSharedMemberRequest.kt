package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorSerializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toMemberSelector

sealed interface DeleteNodeSharedMemberRequest {
    val member: MemberSelector
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class DeleteFileSharedMemberRequest(
    @JsonProperty("file")
    val fileId: String,
    private val memberId: String
) : DeleteNodeSharedMemberRequest {
    @Keep
    @JsonProperty("member")
    @JsonSerialize(using = MemberSelectorSerializer::class)
    @JsonDeserialize(using = MemberSelectorDeserializer::class)
    override val member: MemberSelector = memberId.toMemberSelector()
}

data class DeleteFolderSharedMemberRequest(
    @JsonProperty("leave_a_copy")
    val leaveACopy: Boolean = false,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String,
    private val memberId: String
) : DeleteNodeSharedMemberRequest {
    @Keep
    @JsonProperty("member")
    @JsonSerialize(using = MemberSelectorSerializer::class)
    @JsonDeserialize(using = MemberSelectorDeserializer::class)
    override val member: MemberSelector = memberId.toMemberSelector()
}
