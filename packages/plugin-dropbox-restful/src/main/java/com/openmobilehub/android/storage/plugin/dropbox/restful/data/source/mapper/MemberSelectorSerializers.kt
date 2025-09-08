package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.openmobilehub.android.storage.core.model.OmhPermissionRole

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class MemberSelector(
    @JsonProperty(".tag")
    val tag: String,
    @JsonProperty("email")
    val email: String? = null,
    @JsonProperty("dropbox_id")
    val userId: String? = null
)

/**
 * Represents a member with their access level for Dropbox sharing APIs.
 * This follows the Dropbox API structure where each member has their own access_level.
 */
@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
data class AddFolderMember(
    @JsonProperty("member")
    val member: MemberSelector,
    @JsonProperty("access_level")
    @JsonSerialize(using = OmhPermissionRoleSerializer::class)
    @JsonDeserialize(using = OmhPermissionRoleDeserializer::class)
    val accessLevel: OmhPermissionRole
)

internal class MemberSelectorSerializer : JsonSerializer<MemberSelector>() {
    override fun serialize(value: MemberSelector, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField(".tag", value.tag)
        value.email?.let { gen.writeStringField("email", it) }
        value.userId?.let { gen.writeStringField("dropbox_id", it) }
        gen.writeEndObject()
    }
}

internal class MemberSelectorListSerializer : JsonSerializer<List<MemberSelector>>() {
    override fun serialize(value: List<MemberSelector>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        value.forEach { memberSelector ->
            gen.writeStartObject()
            gen.writeStringField(".tag", memberSelector.tag)
            memberSelector.email?.let { gen.writeStringField("email", it) }
            memberSelector.userId?.let { gen.writeStringField("dropbox_id", it) }
            gen.writeEndObject()
        }
        gen.writeEndArray()
    }
}

internal class AddFolderMemberSerializer : JsonSerializer<AddFolderMember>() {
    override fun serialize(value: AddFolderMember, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()

        // Serialize member
        gen.writeFieldName("member")
        gen.writeStartObject()
        gen.writeStringField(".tag", value.member.tag)
        value.member.email?.let { gen.writeStringField("email", it) }
        value.member.userId?.let { gen.writeStringField("dropbox_id", it) }
        gen.writeEndObject()

        // Serialize access level
        gen.writeFieldName("access_level")
        serializers.findValueSerializer(OmhPermissionRole::class.java).serialize(value.accessLevel, gen, serializers)

        gen.writeEndObject()
    }
}

internal class AddFolderMemberListSerializer : JsonSerializer<List<AddFolderMember>>() {
    override fun serialize(value: List<AddFolderMember>, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartArray()
        value.forEach { addMember ->
            gen.writeStartObject()

            // Serialize member
            gen.writeFieldName("member")
            gen.writeStartObject()
            gen.writeStringField(".tag", addMember.member.tag)
            addMember.member.email?.let { gen.writeStringField("email", it) }
            addMember.member.userId?.let { gen.writeStringField("dropbox_id", it) }
            gen.writeEndObject()

            // Serialize access level
            gen.writeFieldName("access_level")
            gen.writeStartObject()
            val accessLevelTag = when (addMember.accessLevel) {
                OmhPermissionRole.OWNER -> "owner"
                OmhPermissionRole.WRITER -> "editor"
                OmhPermissionRole.READER -> "viewer"
                OmhPermissionRole.COMMENTER -> "viewer" // Dropbox doesn't have commenter, map to viewer
            }
            gen.writeStringField(".tag", accessLevelTag)
            gen.writeEndObject()

            gen.writeEndObject()
        }
        gen.writeEndArray()
    }
}

internal class MemberSelectorDeserializer : JsonDeserializer<MemberSelector>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): MemberSelector {
        val node = p.codec.readTree<JsonNode>(p)
        return MemberSelector(
            tag = node.get(".tag").asText(),
            email = node.get("email")?.asText(),
            userId = node.get("dropbox_id")?.asText()
        )
    }
}

internal class MemberSelectorListDeserializer : JsonDeserializer<List<MemberSelector>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<MemberSelector> {
        val node = p.codec.readTree<JsonNode>(p)
        return node.map { element ->
            MemberSelector(
                tag = element.get(".tag").asText(),
                email = element.get("email")?.asText(),
                userId = element.get("dropbox_id")?.asText()
            )
        }
    }
}

internal class AddFolderMemberDeserializer : JsonDeserializer<AddFolderMember>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): AddFolderMember {
        val node = p.codec.readTree<JsonNode>(p)

        // Deserialize member
        val memberNode = node.get("member")
        val member = MemberSelector(
            tag = memberNode.get(".tag").asText(),
            email = memberNode.get("email")?.asText(),
            userId = memberNode.get("dropbox_id")?.asText()
        )

        // Deserialize access level
        val accessLevelNode = node.get("access_level")
        val accessLevel = ctxt.readTreeAsValue(accessLevelNode, OmhPermissionRole::class.java)

        return AddFolderMember(member = member, accessLevel = accessLevel)
    }
}

internal class AddFolderMemberListDeserializer : JsonDeserializer<List<AddFolderMember>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): List<AddFolderMember> {
        val node = p.codec.readTree<JsonNode>(p)
        return node.map { element ->
            // Deserialize member
            val memberNode = element.get("member")
            val member = MemberSelector(
                tag = memberNode.get(".tag").asText(),
                email = memberNode.get("email")?.asText(),
                userId = memberNode.get("dropbox_id")?.asText()
            )

            // Deserialize access level
            val accessLevelNode = element.get("access_level")
            val accessLevel = when (accessLevelNode.get(".tag").asText()) {
                "owner" -> OmhPermissionRole.OWNER
                "editor" -> OmhPermissionRole.WRITER
                "viewer" -> OmhPermissionRole.READER
                else -> throw IllegalArgumentException("Unknown access level: ${accessLevelNode.get(".tag").asText()}")
            }

            AddFolderMember(member = member, accessLevel = accessLevel)
        }
    }
}
