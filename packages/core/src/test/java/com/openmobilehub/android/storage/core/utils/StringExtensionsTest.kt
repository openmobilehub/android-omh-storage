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
    fun `given a string with escaped unicode characters, when unescaping unicode, then return string with actual unicode characters`() {
        // Arrange
        val input = "You don\\u2019t have permission to perform this action."
        val expected = "You don’t have permission to perform this action."

        // Act
        val result = input.unescapeUnicode()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `given a string without escaped unicode, when unescaping unicode, then return original string`() {
        // Arrange
        val input = "Regular string without unicode"
        val expected = "Regular string without unicode"

        // Act
        val result = input.unescapeUnicode()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `given a string with multiple escaped unicode characters, when unescaping unicode, then return string with all characters unescaped`() {
        // Arrange
        val input = "\\u0048\\u0065\\u006c\\u006c\\u006f \\u0057\\u006f\\u0072\\u006c\\u0064"
        val expected = "Hello World"

        // Act
        val result = input.unescapeUnicode()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `test unescapeUnicode with multibyte characters`() {
        val input = "\u3042\u3044\u3046\u3048\u304A"
        val expected = "あいうえお"

        // Act
        val result = input.unescapeUnicode()

        // Assert
        assertEquals(expected, result)
    }

    @Test
    fun `test splitPathToParts`() {
        assertEquals(listOf("abc"), "abc".splitPathToParts())
        assertEquals(listOf(""), "".splitPathToParts())
        assertEquals(listOf(" "), " ".splitPathToParts())
        assertEquals(listOf("a", "b", "c"), "/a/b/c".splitPathToParts())
        assertEquals(listOf("a", "b", "c"), "/a/b/c/".splitPathToParts())
    }

    @Test
    fun `given RFC3339 without fractional seconds, when converted to date, then return correct date`() {
        val input = "2024-05-01T00:00:00Z"
        val expected = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.parse(input)
        val result = input.fromRFC3339StringToDate()
        assertEquals(expected, result)
    }

    @Test
    fun `given RFC3339 with 7 fractional digits, when converted to date, then truncate to millis`() {
        val input = "2024-05-01T00:00:00.1234567Z"
        val expected = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.parse("2024-05-01T00:00:00.123Z")
        val result = input.fromRFC3339StringToDate()
        assertEquals(expected, result)
    }

    @Test
    fun `given RFC3339 with 2 fractional digits, when converted to date, then pad to millis`() {
        val input = "2024-05-01T00:00:00.12Z"
        val expected = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }.parse("2024-05-01T00:00:00.120Z")
        val result = input.fromRFC3339StringToDate()
        assertEquals(expected, result)
    }
}
