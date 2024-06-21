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

import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_JUNE_2024_MILLIS
import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_MAY_2024_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date

class DateUtilsTest {

    @Test
    fun `given a secondDate is after firstDate, when getting newer date, return secondDate`() {
        // Arrange
        val firstDate = Date(TEST_FIRST_MAY_2024_MILLIS) // 1st May 2024
        val secondDate = Date(TEST_FIRST_JUNE_2024_MILLIS) // 1st June 2024

        // Act
        val result = DateUtils.getNewerDate(firstDate, secondDate)

        // Assert
        assertEquals(secondDate, result)
    }

    @Test
    fun `given a secondDate is before firstDate, when getting newer date, return firstDate`() {
        // Arrange
        val firstDate = Date(TEST_FIRST_JUNE_2024_MILLIS) // 1st June 2024
        val secondDate = Date(TEST_FIRST_MAY_2024_MILLIS) // 1st May 2024

        // Act
        val result = DateUtils.getNewerDate(firstDate, secondDate)

        // Assert
        assertEquals(firstDate, result)
    }
}
