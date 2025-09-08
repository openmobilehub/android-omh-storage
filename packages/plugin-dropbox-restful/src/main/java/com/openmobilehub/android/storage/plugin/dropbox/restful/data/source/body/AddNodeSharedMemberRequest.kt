package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.AddFolderMember
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.AddFolderMemberListDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.AddFolderMemberListSerializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorListDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorListSerializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.OmhPermissionRoleDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.OmhPermissionRoleSerializer

@Keep
sealed interface AddNodeSharedMemberRequest {
    val customMessage: String?
    val quiet: Boolean
}

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class AddFileSharedMemberRequest(
    @JsonProperty("members")
    @JsonSerialize(using = MemberSelectorListSerializer::class)
    @JsonDeserialize(using = MemberSelectorListDeserializer::class)
    val members: List<MemberSelector>,
    @JsonProperty("access_level")
    @JsonSerialize(using = OmhPermissionRoleSerializer::class)
    @JsonDeserialize(using = OmhPermissionRoleDeserializer::class)
    val accessLevel: OmhPermissionRole,
    @JsonProperty("add_message_as_comment")
    val addMessageAsComment: Boolean = false,
    @JsonProperty("custom_message")
    override val customMessage: String? = null,
    @JsonProperty("quiet")
    override val quiet: Boolean,
    @JsonProperty("file")
    val fileId: String
) : AddNodeSharedMemberRequest

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class AddFolderSharedMemberRequest(
    @JsonProperty("members")
    @JsonSerialize(using = AddFolderMemberListSerializer::class)
    @JsonDeserialize(using = AddFolderMemberListDeserializer::class)
    val members: List<AddFolderMember>,
    @JsonProperty("custom_message")
    override val customMessage: String? = null,
    @JsonProperty("quiet")
    override val quiet: Boolean,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String
) : AddNodeSharedMemberRequest
