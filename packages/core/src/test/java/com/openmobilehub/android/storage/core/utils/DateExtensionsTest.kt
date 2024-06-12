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
