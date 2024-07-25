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

package com.openmobilehub.android.storage.plugin.dropbox.testdoubles

import com.dropbox.core.v2.sharing.AccessLevel
import com.dropbox.core.v2.sharing.GroupInfo
import com.dropbox.core.v2.sharing.GroupMembershipInfo
import com.dropbox.core.v2.sharing.MemberSelector
import com.dropbox.core.v2.sharing.UserInfo
import com.dropbox.core.v2.sharing.UserMembershipInfo
import io.mockk.every

internal fun UserMembershipInfo.setUpMock(userInfo: UserInfo) {
    every { user } returns userInfo
    every { accessType } returns AccessLevel.VIEWER
    every { isInherited } returns false
}

internal fun UserInfo.setUpMock() {
    every { accountId } returns TEST_PERMISSION_ID
    every { displayName } returns TEST_PERMISSION_DISPLAY_NAME
    every { email } returns TEST_PERMISSION_EMAIL_ADDRESS
}

internal fun GroupMembershipInfo.setUpMock(groupInfo: GroupInfo) {
    every { group } returns groupInfo
    every { accessType } returns AccessLevel.EDITOR
    every { isInherited } returns true
}

internal fun GroupInfo.setUpMock() {
    every { groupId } returns TEST_PERMISSION_ID
    every { groupName } returns TEST_PERMISSION_DISPLAY_NAME
}

internal val emailMemberSelector = MemberSelector.email(TEST_PERMISSION_EMAIL_ADDRESS)
internal val dropboxIdMemberSelector = MemberSelector.dropboxId(TEST_IDENTITY_ID)
