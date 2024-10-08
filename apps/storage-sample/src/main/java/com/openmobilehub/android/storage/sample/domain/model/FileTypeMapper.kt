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

package com.openmobilehub.android.storage.sample.domain.model

object FileTypeMapper {

    private val MAP_FILE_TYPES: Map<String, FileType> =
        FileType.values().associateBy { fileType -> fileType.mimeType }

    fun getFileTypeWithMime(mimeType: String): FileType {
        return if (MAP_FILE_TYPES.containsKey(mimeType)) {
            MAP_FILE_TYPES[mimeType] ?: FileType.OTHER
        } else {
            FileType.OTHER
        }
    }
}
