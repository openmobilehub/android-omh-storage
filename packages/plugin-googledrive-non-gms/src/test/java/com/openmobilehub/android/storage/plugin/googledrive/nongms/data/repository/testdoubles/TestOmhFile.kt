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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles

import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import java.util.Date

const val TEST_FILE_ID = "123"
const val TEST_FILE_NAME = "fileName.txt"
val TEST_FILE_CREATED_TIME = Date(TEST_FIRST_MAY_2024_MILLIS)
val TEST_FILE_MODIFIED_TIME = Date(TEST_FIRST_JUNE_2024_MILLIS)
const val TEST_FILE_PARENT_ID = "parentId"
const val TEST_FILE_MIME_TYPE = "test/mime-type"
const val TEST_FILE_SIZE = 10

val testOmhFile = OmhStorageEntity.OmhStorageFile(
    TEST_FILE_ID,
    TEST_FILE_NAME,
    TEST_FILE_CREATED_TIME,
    TEST_FILE_MODIFIED_TIME,
    TEST_FILE_PARENT_ID,
    TEST_FILE_MIME_TYPE,
    TEST_FILE_SIZE
)
