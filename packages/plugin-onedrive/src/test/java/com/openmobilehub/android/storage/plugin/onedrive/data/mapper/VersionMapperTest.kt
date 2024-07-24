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

package com.openmobilehub.android.storage.plugin.onedrive.data.mapper

import com.microsoft.graph.models.DriveItemVersion
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.onedrive.testdoubles.testOmhVersion
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.MockK
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class VersionMapperTest {

    @MockK
    private lateinit var driveItemVersion: DriveItemVersion

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        driveItemVersion.setUpMock()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a DriveItemVersion with specific properties, when mapped, then return the expected OmhFileVersion`() {
        // Act
        val result = driveItemVersion.toOmhVersion(TEST_VERSION_FILE_ID)

        // Assert
        Assert.assertEquals(testOmhVersion, result)
    }
}
