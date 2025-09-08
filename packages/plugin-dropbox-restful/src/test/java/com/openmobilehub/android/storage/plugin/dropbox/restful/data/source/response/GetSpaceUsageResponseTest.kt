package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSpaceUsageResponseTest {

    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Test
    fun `test GetSpaceResponse`() {
        val source = """
            {
              "used": 123456789,
              "allocation": {
                ".tag": "individual",
                "allocated": 987654321
              }
            }
        """.trimIndent()

        val result: GetSpaceUsageResponse = objectMapper
            .readerFor(GetSpaceUsageResponse::class.java)
            .readValue(source)

        assertEquals(123456789, result.used)
        assertEquals(987654321, result.allocation.allocated)
    }
}
