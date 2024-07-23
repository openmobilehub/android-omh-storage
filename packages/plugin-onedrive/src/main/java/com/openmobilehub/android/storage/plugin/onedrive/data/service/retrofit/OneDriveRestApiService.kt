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

package com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit

import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.body.CreateFolderRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.data.service.retrofit.response.DriveItemResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

@Suppress("TooManyFunctions")
interface OneDriveRestApiService {

    companion object {
        private const val DRIVES_PARTICLE = "drives"

        private const val DRIVE_ID = "driveId"
        private const val PARENT_ID = "parentId"
    }

    @POST("$DRIVES_PARTICLE/{$DRIVE_ID}/items/{$PARENT_ID}/children")
    suspend fun createFolder(
        @Path(DRIVE_ID) driveId: String,
        @Path(PARENT_ID) parentId: String,
        @Body body: CreateFolderRequestBody,
    ): Response<DriveItemResponse>
}
