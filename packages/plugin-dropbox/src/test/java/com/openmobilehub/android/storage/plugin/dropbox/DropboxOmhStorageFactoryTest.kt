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

package com.openmobilehub.android.storage.plugin.dropbox

import com.openmobilehub.android.auth.core.OmhAuthClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class DropboxOmhStorageFactoryTest {

    @MockK
    private lateinit var authClient: OmhAuthClient

    @MockK
    private lateinit var storageClient: DropboxOmhStorageClient

    private lateinit var factory: DropboxOmhStorageFactory

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkConstructor(DropboxOmhStorageClient.Builder::class)
        every { anyConstructed<DropboxOmhStorageClient.Builder>().build(authClient) } returns storageClient

        factory = DropboxOmhStorageFactory()
    }

    @Test
    fun `given DropboxOmhStorageFactory, when getting storage client, then return DropboxOmhStorageClient`() {
        // Act
        val result = factory.getStorageClient(authClient)

        // Assert
        assertSame(storageClient, result)
    }
}
