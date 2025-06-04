package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.AddFolderMember
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toMemberSelector
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test

class AddNodeSharedMemberRequestTest {

    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `test write AddFileSharedMemberRequest to JSON using email`() {
        val s =
            objectMapper.writerFor(AddFileSharedMemberRequest::class.java).writeValueAsString(
                AddFileSharedMemberRequest(
                    members = listOf("test@test.com".toMemberSelector()),
                    accessLevel = OmhPermissionRole.OWNER,
                    fileId = "id:This is a test",
                    quiet = false
                )
            )
        val result = JSONObject(s)
        result.getJSONArray("members").let { members ->
            assertTrue(members.length() == 1)
            members.getJSONObject(0).let { memberObj ->
                assertEquals("email", memberObj.getString(".tag"))
                assertEquals("test@test.com", memberObj.getString("email"))
            }
            val accessLevel = result.getJSONObject("access_level")
            assertEquals("owner", accessLevel.getString(".tag"))
            assertFalse(result.getBoolean("quiet"))
            assertFalse(result.getBoolean("add_message_as_comment"))
            assertEquals("id:This is a test", result.getString("file"))
        }
    }

    @Test
    fun `test write AddFileSharedMemberRequest to JSON using member ID`() {
        val s =
            objectMapper.writerFor(AddFileSharedMemberRequest::class.java).writeValueAsString(
                AddFileSharedMemberRequest(
                    members = listOf("this is a member ID".toMemberSelector()),
                    accessLevel = OmhPermissionRole.OWNER,
                    fileId = "id:This is a test",
                    quiet = false
                )
            )
        val result = JSONObject(s)
        result.getJSONArray("members").let { members ->
            assertTrue(members.length() == 1)
            members.getJSONObject(0).let { memberObj ->
                assertEquals("dropbox_id", memberObj.getString(".tag"))
                assertEquals("this is a member ID", memberObj.getString("dropbox_id"))
            }
            val accessLevel = result.getJSONObject("access_level")
            assertEquals("owner", accessLevel.getString(".tag"))
            assertFalse(result.getBoolean("quiet"))
            assertFalse(result.getBoolean("add_message_as_comment"))
            assertEquals("id:This is a test", result.getString("file"))
        }
    }

    @Test
    fun `test write AddFolderSharedMemberRequest to JSON using email`() {
        val s =
            objectMapper.writerFor(AddFolderSharedMemberRequest::class.java).writeValueAsString(
                AddFolderSharedMemberRequest(
                    members = listOf(
                        AddFolderMember(
                            member = "test@test.com".toMemberSelector(),
                            accessLevel = OmhPermissionRole.OWNER
                        )
                    ),
                    sharedFolderId = "id:This is a folder id",
                    quiet = false
                )
            )
        val result = JSONObject(s)
        result.getJSONArray("members").let { members ->
            assertTrue(members.length() == 1)
            members.getJSONObject(0).let { memberObj ->
                val member = memberObj.getJSONObject("member")
                assertEquals("email", member.getString(".tag"))
                assertEquals("test@test.com", member.getString("email"))
                val accessLevel = memberObj.getJSONObject("access_level")
                assertEquals("owner", accessLevel.getString(".tag"))
            }
            assertFalse(result.getBoolean("quiet"))
            assertEquals("id:This is a folder id", result.getString("shared_folder_id"))
        }
    }

    @Test
    fun `test write AddFolderSharedMemberRequest to JSON using member ID`() {
        val s =
            objectMapper.writerFor(AddFolderSharedMemberRequest::class.java).writeValueAsString(
                AddFolderSharedMemberRequest(
                    members = listOf(
                        AddFolderMember(
                            member = "dropbox member ID".toMemberSelector(),
                            accessLevel = OmhPermissionRole.OWNER
                        )
                    ),
                    sharedFolderId = "id:This is a folder id",
                    quiet = false
                )
            )
        val result = JSONObject(s)
        result.getJSONArray("members").let { members ->
            assertTrue(members.length() == 1)
            members.getJSONObject(0).let { memberObj ->
                val member = memberObj.getJSONObject("member")
                assertEquals("dropbox_id", member.getString(".tag"))
                assertEquals("dropbox member ID", member.getString("dropbox_id"))
                val accessLevel = memberObj.getJSONObject("access_level")
                assertEquals("owner", accessLevel.getString(".tag"))
            }
            assertFalse(result.getBoolean("quiet"))
            assertEquals("id:This is a folder id", result.getString("shared_folder_id"))
        }
    }

    @Test
    fun `test unmarshall JSON to AddFileSharedMemberRequest`() {
        val source = """
        {
            "file": "/test.txt",
            "members": [
                {
                    ".tag": "dropbox_id",
                    "dropbox_id": "123456"
                }
            ],
            "access_level": {
                ".tag": "owner"
            },
            "custom_message": "This is a test",
            "quiet": false,
            "add_message_as_comment": false
        }
        """.trimIndent()
        val result: AddFileSharedMemberRequest =
            objectMapper
                .readerFor(AddFileSharedMemberRequest::class.java)
                .readValue(source)
        assertEquals("123456", result.members[0].userId)
        assertEquals("This is a test", result.customMessage)
        assertEquals(OmhPermissionRole.OWNER, result.accessLevel)
    }
}
