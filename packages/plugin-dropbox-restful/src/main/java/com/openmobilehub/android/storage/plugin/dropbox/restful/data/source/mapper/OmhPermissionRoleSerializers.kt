package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.openmobilehub.android.storage.core.model.OmhPermissionRole

fun OmhPermissionRole.toAccessLevel(): String {
    return when (this) {
        OmhPermissionRole.OWNER -> "owner"
        OmhPermissionRole.WRITER -> "editor"
        OmhPermissionRole.READER -> "viewer_no_comment"
        OmhPermissionRole.COMMENTER -> "viewer"
    }
}

internal class OmhPermissionRoleSerializer : JsonSerializer<OmhPermissionRole>() {
    override fun serialize(value: OmhPermissionRole, gen: JsonGenerator, serializers: SerializerProvider) {
        val accessLevel = value.toAccessLevel()
        gen.writeStartObject()
        gen.writeStringField(".tag", accessLevel)
        gen.writeEndObject()
    }
}

internal class OmhPermissionRoleDeserializer : JsonDeserializer<OmhPermissionRole>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OmhPermissionRole? {
        val node = p.codec.readTree<JsonNode>(p)
        val tag = node.get(".tag").asText()
        return when (tag) {
            "owner" -> OmhPermissionRole.OWNER
            "editor" -> OmhPermissionRole.WRITER
            "viewer" -> OmhPermissionRole.COMMENTER
            "viewer_no_comment" -> OmhPermissionRole.READER
            else -> throw IllegalArgumentException("Unknown permission role: $tag")
        }
    }
}
