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

sealed class OmhStorageException(private val statusCode: Int) : Exception() {

    override val message: String?
        get() = OmhStorageStatusCodes.getStatusCodeString(statusCode)

    class InvalidCredentialsException(statusCode: Int) : OmhStorageException(statusCode)

    class ApiException(statusCode: Int, override val cause: Throwable? = null) : OmhStorageException(statusCode)

    class DownloadException(
        statusCode: Int,
        override val cause: Throwable?
    ) : OmhStorageException(statusCode)

    class UpdateException(
        statusCode: Int,
        override val cause: Throwable?
    ) : OmhStorageException(statusCode)

    class NotSupportedException(
        statusCode: Int,
        override val cause: Throwable?
    ) : OmhStorageException(statusCode)
}
