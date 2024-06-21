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

package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.Revision
import io.mockk.every

fun Revision.setUpMock() {
    every { id } returns TEST_VERSION_ID
    every { modifiedTime } returns DateTime(TEST_VERSION_FILE_MODIFIED_TIME)
}
