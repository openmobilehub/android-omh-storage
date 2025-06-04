package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import androidx.annotation.Keep
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

@Keep
data class ListFolderResponse(
    val cursor: String,
    val hasMore: Boolean,
    val entries: List<NodeMetadata>
)

class ListFolderResponseDeserializer : JsonDeserializer<ListFolderResponse>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ListFolderResponse {
        val node = p.codec.readTree<JsonNode>(p)
        val hasMore = node.get("has_more").asBoolean(false)
        val cursor = node.get("cursor").asText(null)
        val entries: List<NodeMetadata> = node.get("entries").asIterable().map { item ->
            val tag = item.get(".tag").asText()
            when (tag) {
                "file" -> {
                    FileMetadata(
                        item.get(".tag").asText(),
                        item.get("content_hash").asText(),
                        item.get("has_explicit_shared_members")?.asBoolean(false) ?: false,
                        item.get("id").asText(),
                        item.get("is_downloadable").asBoolean(),
                        item.get("name").asText(),
                        item.get("path_display").asText(),
                        item.get("size").asLong(),
                        item.get("client_modified").asText(null),
                        item.get("server_modified").asText(null),
                        null,
                        item.get("rev").asText(null)
                    )
                }
                "folder" -> {
                    FolderMetadata(
                        item.get(".tag").asText(),
                        item.get("id").asText(),
                        item.get("name").asText(),
                        item.get("path_display").asText(),
                        null
                    )
                }
                else -> throw IllegalArgumentException("Unsupported tag: $tag")
            }
        }
        return ListFolderResponse(cursor, hasMore, entries)
    }
}
