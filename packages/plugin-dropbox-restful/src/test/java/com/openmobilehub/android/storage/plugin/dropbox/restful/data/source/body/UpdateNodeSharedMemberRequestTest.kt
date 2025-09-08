package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toMemberSelector
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class UpdateNodeSharedMemberRequestTest {
    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `test UpdateFileSharedMemberRequest to JSON`() {
        val source = UpdateFileSharedMemberRequest(
            accessLevel = OmhPermissionRole.OWNER,
            fileId = "id:test file id 1",
            member = "testuser@user.email".toMemberSelector()
        )

        val result = JSONObject(
            objectMapper.writerFor(UpdateFileSharedMemberRequest::class.java)
                .writeValueAsString(source)
        )
        assertEquals("email", result.getJSONObject("member").getString(".tag"))
        assertEquals("testuser@user.email", result.getJSONObject("member").getString("email"))
        assertEquals("owner", result.getJSONObject("access_level").getString(".tag"))
    }

    @Test
    fun `test unmarshall JSON to UpdateFileSharedMemberRequest`() {
        val source = """
        {
            "file": "id:test file id",
            "member": {".tag":"email","email":"test@test.com"},
            "access_level": {".tag":"viewer_no_comment"}
        }
        """.trimIndent()

        val result: UpdateFileSharedMemberRequest =
            objectMapper
                .readerFor(UpdateFileSharedMemberRequest::class.java)
                .readValue(source)

        assertEquals("test@test.com", result.member.email)
        assertEquals(OmhPermissionRole.READER, result.accessLevel)
    }
}
