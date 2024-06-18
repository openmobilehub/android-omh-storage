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

package com.openmobilehub.android.storage.core.model

import java.util.Date

sealed class OmhStorageEntity(
    open val id: String,
    open val name: String,
    open val createdTime: Date?,
    open val modifiedTime: Date?,
    open val parentId: String?,
    open val originalMetadata: Any?
) {
    data class OmhStorageFile(
        override val id: String,
        override val name: String,
        override val createdTime: Date?,
        override val modifiedTime: Date?,
        override val parentId: String?,
        override val originalMetadata: Any?,
        val mimeType: String?,
        val size: Int?,
        val extension: String?
    ) : OmhStorageEntity(
        id,
        name,
        createdTime,
        modifiedTime,
        parentId,
        originalMetadata
    )

    data class OmhStorageFolder(
        override val id: String,
        override val name: String,
        override val createdTime: Date?,
        override val modifiedTime: Date?,
        override val parentId: String?,
        override val originalMetadata: Any?,
    ) : OmhStorageEntity(
        id,
        name,
        createdTime,
        modifiedTime,
        parentId,
        originalMetadata
    )
}
