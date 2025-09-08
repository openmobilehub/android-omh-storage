package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.MemberSelectorSerializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.OmhPermissionRoleDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.OmhPermissionRoleSerializer

@Keep
sealed interface UpdateNodeSharedMemberRequest {
    val accessLevel: OmhPermissionRole
    val member: MemberSelector
}

@Keep
data class UpdateFileSharedMemberRequest(
    @JsonProperty("access_level")
    @JsonSerialize(using = OmhPermissionRoleSerializer::class)
    @JsonDeserialize(using = OmhPermissionRoleDeserializer::class)
    override val accessLevel: OmhPermissionRole,
    @JsonProperty("member")
    @JsonSerialize(using = MemberSelectorSerializer::class)
    @JsonDeserialize(using = MemberSelectorDeserializer::class)
    override val member: MemberSelector,
    @JsonProperty("file")
    val fileId: String
) : UpdateNodeSharedMemberRequest

@Keep
data class UpdateFolderSharedMemberRequest(
    @JsonProperty("access_level")
    @JsonSerialize(using = OmhPermissionRoleSerializer::class)
    @JsonDeserialize(using = OmhPermissionRoleDeserializer::class)
    override val accessLevel: OmhPermissionRole,
    @JsonProperty("member")
    @JsonSerialize(using = MemberSelectorSerializer::class)
    @JsonDeserialize(using = MemberSelectorDeserializer::class)
    override val member: MemberSelector,
    @JsonProperty("shared_folder_id")
    val sharedFolderId: String
) : UpdateNodeSharedMemberRequest
