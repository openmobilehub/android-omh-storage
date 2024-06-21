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

package com.openmobilehub.android.storage.plugin.onedrive

import com.openmobilehub.android.auth.core.OmhAuthClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class OneDriveOmhStorageFactoryTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var storageClient: OneDriveOmhStorageClient

    private lateinit var factory: OneDriveOmhStorageFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(OneDriveOmhStorageClient.Builder::class)
        every { anyConstructed<OneDriveOmhStorageClient.Builder>().build(authClient) } returns storageClient

        factory = OneDriveOmhStorageFactory()
    }

    @Test
    fun `given OneDriveOmhStorageFactory, when getting storage client, then return OneDriveOmhStorageClient`() {
        // Act
        val result = factory.getStorageClient(authClient)

        // Assert
        assertSame(storageClient, result)
    }
}
