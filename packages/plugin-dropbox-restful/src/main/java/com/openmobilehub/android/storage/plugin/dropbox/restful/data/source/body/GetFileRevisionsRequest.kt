package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize

@Keep
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = GetFileRevisionsSerializer::class)
data class GetFileRevisionsRequest(
    @JsonProperty("path")
    val path: String,
    @JsonProperty("limit")
    val limit: Int? = 100,
    val mode: String? = "id",
)

internal class GetFileRevisionsSerializer : JsonSerializer<GetFileRevisionsRequest>() {
    override fun serialize(
        value: GetFileRevisionsRequest,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeStartObject()
        gen.writeStringField("path", value.path)
        value.limit?.let { gen.writeNumberField("limit", it) }
        value.mode?.let {
            gen.writeObjectFieldStart("mode")
            gen.writeStringField(".tag", it)
            gen.writeEndObject()
        }
        gen.writeEndObject()
    }
}
