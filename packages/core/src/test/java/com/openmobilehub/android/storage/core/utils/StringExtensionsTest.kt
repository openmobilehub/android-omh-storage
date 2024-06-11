package com.openmobilehub.android.storage.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
