/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles

import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import org.json.JSONArray
import org.json.JSONObject

object TestPermissionResponse {

    const val TEST_PERMISSION_USER_ID = "user123"
    const val TEST_PERMISSION_USER_NAME = "John Doe"
    const val TEST_PERMISSION_USER_EMAIL = "john.doe@example.com"

    const val TEST_PERMISSION_GROUP_ID = "group456"
    const val TEST_PERMISSION_GROUP_NAME = "Test Group"

    // Test file shared members response JSON
    val testFileSharedMembersResponseJson = JSONObject().apply {
        put(
            "users",
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        // Dropbox user permission objects expose access_type with nested .tag for the role
                        put(
                            "access_type",
                            JSONObject().apply { put(".tag", "viewer_no_comment") }
                        )
                        put("is_inherited", false)
                        put(
                            "user",
                            JSONObject().apply {
                                put("account_id", TEST_PERMISSION_USER_ID)
                                put("display_name", TEST_PERMISSION_USER_NAME)
                                put("email", TEST_PERMISSION_USER_EMAIL)
                            }
                        )
                    }
                )
            }
        )
        put(
            "groups",
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("id", TEST_PERMISSION_GROUP_ID)
                        put("role", "editor")
                        put("is_inherited", false)
                        put(
                            "group",
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("name", TEST_PERMISSION_GROUP_NAME)
                            }
                        )
                    }
                )
            }
        )
    }

    // Test folder shared members response JSON
    val testFolderSharedMembersResponseJson = JSONObject().apply {
        put(
            "users",
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        put(
                            "access_type",
                            JSONObject().apply { put(".tag", "viewer_no_comment") }
                        )
                        put("is_inherited", false)
                        put(
                            "user",
                            JSONObject().apply {
                                put("account_id", TEST_PERMISSION_USER_ID)
                                put("display_name", TEST_PERMISSION_USER_NAME)
                                put("email", TEST_PERMISSION_USER_EMAIL)
                            }
                        )
                    }
                )
            }
        )
        put(
            "groups",
            JSONArray().apply {
                put(
                    JSONObject().apply {
                        put("id", TEST_PERMISSION_GROUP_ID)
                        put("role", "editor")
                        put("is_inherited", false)
                        put(
                            "group",
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("name", TEST_PERMISSION_GROUP_NAME)
                            }
                        )
                    }
                )
            }
        )
    }

    // Expected OmhPermission objects for testing
    val testOmhUserPermission = OmhPermission.IdentityPermission(
        id = TEST_PERMISSION_USER_ID,
        role = OmhPermissionRole.READER,
        isInherited = false,
        identity = OmhIdentity.User(
            id = TEST_PERMISSION_USER_ID,
            displayName = TEST_PERMISSION_USER_NAME,
            emailAddress = TEST_PERMISSION_USER_EMAIL,
            expirationTime = null,
            deleted = null,
            photoLink = null,
            pendingOwner = null
        )
    )

    val testOmhGroupPermission = OmhPermission.IdentityPermission(
        id = TEST_PERMISSION_GROUP_ID,
        role = OmhPermissionRole.WRITER,
        isInherited = false,
        identity = OmhIdentity.Group(
            id = TEST_PERMISSION_GROUP_ID,
            displayName = TEST_PERMISSION_GROUP_NAME,
            emailAddress = null,
            expirationTime = null,
            deleted = null
        )
    )

    val testExpectedPermissions = listOf(testOmhGroupPermission, testOmhUserPermission)
}
