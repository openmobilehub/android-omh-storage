package com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository

import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.FileVersionResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.FileVersionsListResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class OneDriveRestfulFileRepositoryVersionsTest {

    private val api: OneDriveApiService = mockk()
    private val httpClient = OkHttpClient.Builder().build()
    private val repository = OneDriveRestfulFileRepository(api, httpClient)

    @Test
    fun `getItemVersions returns all versions even with varying fractional seconds`() = runTest {
        // Given: versions with 7-digit, 3-digit, 0-digit and 2-digit fractions
        val versions = listOf(
            FileVersionResponse(id = "1", lastModifiedDateTime = "2024-05-01T00:00:00.1234567Z"),
            FileVersionResponse(id = "2", lastModifiedDateTime = "2024-05-01T00:00:01.123Z"),
            FileVersionResponse(id = "3", lastModifiedDateTime = "2024-05-01T00:00:02Z"),
            FileVersionResponse(id = "4", lastModifiedDateTime = "2024-05-01T00:00:03.12Z"),
        )
        coEvery {
            api.getItemVersions("file123")
        } returns Response.success(FileVersionsListResponse(versions))

        // When
        val result: List<OmhFileVersion> = repository.getItemVersions("file123")

        // Then
        assertEquals(4, result.size)
        assertTrue(result.all { it.lastModified != null })
        assertEquals(listOf("1", "2", "3", "4"), result.map { it.versionId })
    }
}
