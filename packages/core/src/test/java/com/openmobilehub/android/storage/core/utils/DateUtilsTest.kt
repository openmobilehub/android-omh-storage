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
