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

@file:Suppress("MaximumLineLength", "MaxLineLength")

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
    fun `given a string with special characters, when removing special characters, then return a string without special characters`() {
        // Arrange
        val input = "fileśćżźę@0#2.txt"
        val expected = "file______0_2.txt"

        // Act
        val result = input.removeSpecialCharacters()

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

    @Test
    fun `test splitPathToParts`() {
        assertEquals(listOf("abc"), "abc".splitPathToParts())
        assertEquals(listOf(""), "".splitPathToParts())
        assertEquals(listOf(" "), " ".splitPathToParts())
        assertEquals(listOf("a", "b", "c"), "/a/b/c".splitPathToParts())
        assertEquals(listOf("a", "b", "c"), "/a/b/c/".splitPathToParts())
    }
}
