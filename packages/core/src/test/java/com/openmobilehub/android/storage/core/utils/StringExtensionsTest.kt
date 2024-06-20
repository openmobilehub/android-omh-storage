package com.openmobilehub.android.storage.core.utils

import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_MAY_2024_MILLIS
import com.openmobilehub.android.storage.core.testdoubles.TEST_FIRST_MAY_2024_RFC_3339
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

class StringExtensionsTest {

    @Test
    fun `given a string with whitespaces, when removing whitespaces, then return a string without whitespaces`() {
        // Arrange
        val input = "file  0 2.txt"
        val expected = "file__0_2.txt"

        // Act
        val result = input.removeWhitespaces()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `given RFC3339 formatted string, when converted to date, then return correct date`() {
        // Arrange
        val input = TEST_FIRST_MAY_2024_RFC_3339
        val expected = Date(TEST_FIRST_MAY_2024_MILLIS)

        // Act
        val result = input.fromRFC3339StringToDate()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `given non-RFC3339 formatted string, when converted to date, then return null`() {
        // Arrange
        val input = "2022-01-01 00:00:00"

        // Act
        val result = input.fromRFC3339StringToDate()

        // Assert
        assertNull(result)
    }

    @Test
    fun `given empty string, when converted to date, then return null`() {
        // Arrange
        val input = ""

        // Act
        val result = input.fromRFC3339StringToDate()

        // Assert
        assertNull(result)
    }
}
