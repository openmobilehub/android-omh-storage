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

package com.openmobilehub.android.storage.core.utils

import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_MAY_2024_MILLIS
import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_MAY_2024_RFC_3339
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class DateExtensionsTest {

    @Test
    fun `given a Date object, when converted to RFC3339, then returns a string in correct format`() {
        // Arrange
        val date = Date(TEST_FIRST_MAY_2024_MILLIS)

        // Act
        val result = date.toRFC3339String()

        // Assert
        assertEquals(TEST_FIRST_MAY_2024_RFC_3339, result)
    }
}
