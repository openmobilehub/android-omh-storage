package com.openmobilehub.android.storage.plugin.dropbox.restful.data.repository

import android.webkit.MimeTypeMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.restful.common.data.mapper.LocalFileToMimeType
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxContentApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.retrofit.DropboxRetrofitImpl
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AppendUploadSessionRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CheckShareJobStatusRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ContinueRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CreateFolderRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFolderSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ExportFileRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.FinishUploadSessionRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.GetFileRevisionsRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFileSharedMembersRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFolderRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFolderSharedMembersRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.NodeMetadataRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.PathRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.SearchFileRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ShareFolderRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.UploadSessionCursor
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.CreateFolderResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderSharingInfo
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFileRevisionsResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ShareJobResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SharedFileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SharedFolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.UploadSessionResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestFileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestFileMetadata.testFileUploadInChunksResult
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestFolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestGetSpaceUsageResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestListFolderResponse.testFile1
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestListFolderResponse.testFile2
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestListFolderResponse.testFileListResponseWithNextCursor
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestListFolderResponse.testFileListResponseWithNextCursor2ndResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestListFolderResponse.testFileListResponseWithoutNextCursor
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.TEST_PERMISSION_GROUP_ID
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.TEST_PERMISSION_GROUP_NAME
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.TEST_PERMISSION_USER_EMAIL
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.TEST_PERMISSION_USER_ID
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.TEST_PERMISSION_USER_NAME
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.testExpectedPermissions
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.testFileSharedMembersResponseJson
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.testFolderSharedMembersResponseJson
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.testOmhGroupPermission
import com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles.TestPermissionResponse.testOmhUserPermission
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File
import java.io.FileInputStream
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

@Suppress(
    "LargeClass",
    "UnusedPrivateMember",
    "ForbiddenComment",
    "MaxLineLength",
    "MaximumLineLength",
    "LongMethod"
)
@OptIn(ExperimentalCoroutinesApi::class)
class DropboxRestfulFileRepositoryTest {

    companion object {
        private const val TEST_MIME_TYPE = "application/x-test-mimetype"

        /**
         * Creates a ResponseBody that can be consumed multiple times for testing.
         * This is needed because ResponseBody.string() can only be called once.
         */
        private fun createReusableResponseBody(content: String, mediaType: String = "application/json"): ResponseBody {
            return object : ResponseBody() {
                override fun contentType() = mediaType.toMediaTypeOrNull()
                override fun contentLength() = content.length.toLong()
                override fun source() = Buffer().writeUtf8(content)
            }
        }
    }

    private val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @MockK(relaxed = true)
    private lateinit var retrofitImpl: DropboxRetrofitImpl

    @MockK(relaxed = true)
    private lateinit var dropboxApiService: DropboxApiService

    @MockK(relaxed = true)
    private lateinit var dropboxContentApiService: DropboxContentApiService

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK(relaxed = true)
    private lateinit var responseBody: ResponseBody

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK(relaxed = true)
    private lateinit var fileInputStream: FileInputStream

    @MockK
    private lateinit var localFileToMimeType: LocalFileToMimeType

    private lateinit var fileRepositoryImpl: DropboxRestfulFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { localFileToMimeType.invoke(any()) } returns TEST_MIME_TYPE

        fileRepositoryImpl = DropboxRestfulFileRepository(
            dropboxApiService,
            dropboxContentApiService,
            smallFileLimitInMB = 1 // Lower the bar to force upload session and upload in chunks
        )

        every { retrofitImpl.dropboxApiService } returns dropboxApiService
        every { retrofitImpl.dropboxContentApiService } returns dropboxContentApiService
        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        every { mimeTypeMap.getMimeTypeFromExtension(any()) } returns TEST_MIME_TYPE
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given a blank parentId, when getFilesList is success, then a list of OmhStorageEntities is returned`() =
        runTest {
            coEvery { dropboxApiService.getFilesList(any<ListFolderRequestBody>()) } returns Response.success(
                testFileListResponseWithoutNextCursor
            )

            val request = ListFolderRequestBody(
                path = "",
                includeDeletedFiles = false,
                includeHasExplicitSharedMembers = false,
                includeMountedFolders = true,
                includeNonDownloadableFiles = true,
                recursive = false
            )
            val result = fileRepositoryImpl.getFilesList("")

            assertEquals(listOf(testFile1.toOmhFile("")), result)
            coVerify { dropboxApiService.getFilesList(eq(request)) }
        }

    @Test
    fun `test getFilesList with hasMore in response should continue list internally`() =
        runTest {
            coEvery {
                dropboxApiService.getFilesList(any<ListFolderRequestBody>())
            } returns Response.success(testFileListResponseWithNextCursor)
            coEvery {
                dropboxApiService.continueGetFilesList(any<ContinueRequest>())
            } returns Response.success(testFileListResponseWithNextCursor2ndResponse)

            val request = ListFolderRequestBody(
                path = "",
                includeDeletedFiles = false,
                includeHasExplicitSharedMembers = false,
                includeMountedFolders = true,
                includeNonDownloadableFiles = true,
                recursive = false
            )
            val result = fileRepositoryImpl.getFilesList("")

            assertEquals(
                listOf(
                    testFile1.toOmhFile(""),
                    testFile2.toOmhFile("")
                ),
                result
            )
            coVerify { dropboxApiService.getFilesList(eq(request)) }
            coVerify {
                dropboxApiService.continueGetFilesList(
                    eq(ContinueRequest("test cursor 2"))
                )
            }
        }

    @Test
    fun `test getFilesList with empty response`() =
        runTest {
            coEvery { dropboxApiService.getFilesList(any<ListFolderRequestBody>()) } returns Response.success(
                ListFolderResponse(
                    cursor = "test cursor",
                    hasMore = false,
                    entries = emptyList()
                )
            )

            val result = fileRepositoryImpl.getFilesList("")

            assertEquals(emptyList<OmhStorageEntity.OmhFile>(), result)
        }

    @Test
    fun `test downloadFile`() =
        runTest {
            val fileMetadata = objectMapper.writerFor(FileMetadata::class.java)
                .writeValueAsString(testFile1)
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                fileMetadata.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery {
                dropboxContentApiService.downloadFile(any())
            } returns Response.success(
                "12345678".toResponseBody("text/plain".toMediaTypeOrNull()),
                Headers.headersOf("dropbox-api-result", fileMetadata)
            )

            val result = fileRepositoryImpl.downloadFile("id:testFile1")
            assertNotNull(result)
            coVerify {
                dropboxContentApiService.downloadFile(
                    eq(objectMapper.writeValueAsString(PathRequestBody("id:testFile1")))
                )
            }
        }

    @Test
    fun `given downloadFile fails, when called, then throws ApiException`() =
        runTest {
            coEvery {
                dropboxContentApiService.downloadFile(any())
            } returns Response.error(
                404,
                "File not found".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.downloadFile("id:nonExistentFile")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxContentApiService.downloadFile(
                    eq(objectMapper.writeValueAsString(PathRequestBody("id:nonExistentFile")))
                )
            }
        }

    @Test
    fun `test upload big file should cause upload session and upload in chunks`() =
        runTest {
            // Lower the throttle to trigger upload in chunks as much as possible
            val field = DropboxRestfulFileRepository::class.java.getDeclaredField("smallFileLimit")
            field.isAccessible = true
            field.set(fileRepositoryImpl, 10)

            val content = "abcdefghijklmnopqrstuvwxyz1234567890"
            val file = File.createTempFile("upload", ".txt")
            file.deleteOnExit()
            file.writeText(content)

            coEvery {
                dropboxContentApiService.createUploadFileSession(any())
            } returns Response.success(UploadSessionResponse("pid_upload_session:session id"))

            coEvery {
                dropboxContentApiService.continueUploadFile(
                    objectMapper.writer().writeValueAsString(
                        AppendUploadSessionRequest(
                            close = false,
                            cursor =
                            UploadSessionCursor(
                                offset = 0,
                                sessionId = "pid_upload_session:session id",
                            ),
                        ),
                    ),
                    any(),
                )
            } returns Response.success(200, Unit)

            coEvery {
                dropboxContentApiService.continueUploadFile(
                    objectMapper.writer().writeValueAsString(
                        AppendUploadSessionRequest(
                            close = false,
                            cursor =
                            UploadSessionCursor(
                                offset = 10,
                                sessionId = "pid_upload_session:session id",
                            ),
                        ),
                    ),
                    any(),
                )
            } returns Response.success(200, Unit)

            coEvery {
                dropboxContentApiService.continueUploadFile(
                    objectMapper.writer().writeValueAsString(
                        AppendUploadSessionRequest(
                            close = false,
                            cursor =
                            UploadSessionCursor(
                                offset = 20,
                                sessionId = "pid_upload_session:session id",
                            ),
                        ),
                    ),
                    any(),
                )
            } returns Response.success(200, Unit)

            coEvery {
                dropboxContentApiService.continueUploadFile(
                    objectMapper.writer().writeValueAsString(
                        AppendUploadSessionRequest(
                            close = false,
                            cursor =
                            UploadSessionCursor(
                                offset = 30,
                                sessionId = "pid_upload_session:session id",
                            ),
                        ),
                    ),
                    any(),
                )
            } returns Response.success(200, Unit)

            coEvery {
                dropboxContentApiService.finishUploadFile(any(), any())
            } returns Response.success(
                testFileUploadInChunksResult.copy(
                    name = file.name,
                    size = file.length(),
                    id = "id:created file id"
                )
            )

            val result = fileRepositoryImpl.uploadFile(file, "")

            file.delete()

            assertNotNull(result)
            assertEquals(file.name, result?.name)
            assertEquals(content.length, result?.size)
            assertEquals("id:created file id", result?.id)

            coVerify(exactly = 1) {
                dropboxContentApiService.continueUploadFile(
                    withArg {
                        val req = objectMapper.readerFor(AppendUploadSessionRequest::class.java)
                            .readValue<AppendUploadSessionRequest>(it)
                        assertEquals("pid_upload_session:session id", req.cursor.sessionId)
                        assertEquals(0, req.cursor.offset)
                        assertFalse(req.close == true)
                    },
                    withArg { filePart ->
                        assertEquals(10, filePart.contentLength())
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxContentApiService.continueUploadFile(
                    withArg {
                        val req = objectMapper.readerFor(AppendUploadSessionRequest::class.java)
                            .readValue<AppendUploadSessionRequest>(it)
                        assertEquals("pid_upload_session:session id", req.cursor.sessionId)
                        assertEquals(10, req.cursor.offset)
                        assertFalse(req.close == true)
                    },
                    withArg { filePart ->
                        assertEquals(10, filePart.contentLength())
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxContentApiService.continueUploadFile(
                    withArg {
                        val req = objectMapper.readerFor(AppendUploadSessionRequest::class.java)
                            .readValue<AppendUploadSessionRequest>(it)
                        assertEquals("pid_upload_session:session id", req.cursor.sessionId)
                        assertEquals(20, req.cursor.offset)
                        assertFalse(req.close == true)
                    },
                    withArg { filePart ->
                        assertEquals(10, filePart.contentLength())
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxContentApiService.continueUploadFile(
                    withArg {
                        val req = objectMapper.readerFor(AppendUploadSessionRequest::class.java)
                            .readValue<AppendUploadSessionRequest>(it)
                        assertEquals("pid_upload_session:session id", req.cursor.sessionId)
                        assertEquals(30, req.cursor.offset)
                        assertFalse(req.close == true)
                    },
                    withArg { filePart ->
                        assertEquals(6, filePart.contentLength())
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxContentApiService.finishUploadFile(
                    withArg {
                        val orig = objectMapper.readerFor(FinishUploadSessionRequestBody::class.java)
                            .readValue<FinishUploadSessionRequestBody>(it)
                        assertEquals("/${file.name}", orig.createFileRequestBody.path)
                        assertEquals("add", orig.createFileRequestBody.mode)
                    },
                    withArg { filePart ->
                        assertEquals(0, filePart.contentLength())
                    }
                )
            }
        }

    @Test
    fun `test deleteFile`() =
        runTest {
            coEvery { dropboxApiService.deleteFile(any()) } returns Response.success(testFile1)

            val result = fileRepositoryImpl.deleteFile("id:testFile1")
            assertTrue(result)
            coVerify {
                dropboxApiService.deleteFile(eq(PathRequestBody("id:testFile1")))
            }
        }

    @Test
    fun `test get user quota`() =
        runTest {
            coEvery { dropboxApiService.getSpaceUsage() } returns
                Response.success(TestGetSpaceUsageResponse.getSpaceUsageResponseWithQuotaImposed)

            val result = fileRepositoryImpl.getSpaceUsage()
            assertEquals(123456789L, result.used)
            assertEquals(987654321L, result.allocation.allocated)
        }

    // Additional test stubs for comprehensive coverage

    @Test
    fun `given valid folder name and parentId, when createFolder is success, then returns OmhFolder`() =
        runTest {
            val parentFolderResponse = objectMapper.writerFor(FolderMetadata::class.java).writeValueAsString(
                TestFolderMetadata.testParentFolder
            ).toResponseBody(
                "application/json".toMediaTypeOrNull()
            )
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(parentFolderResponse)
            coEvery {
                dropboxApiService.createFolder(
                    CreateFolderRequest(
                        path = "/test parent folder/test folder"
                    )
                )
            } returns Response.success(
                CreateFolderResponse(
                    TestFolderMetadata.testFolder.copy(
                        name = "test folder",
                        path = "${TestFolderMetadata.testParentFolder.path}/test folder"
                    )
                )
            )

            val result = fileRepositoryImpl.createFolder("test folder", "id:test parent folder id")
            assertNotNull(result)
            assertEquals("test folder", result?.name)

            coVerify(exactly = 1) {
                dropboxApiService.getNodeMetaData(
                    withArg { request ->
                        assertEquals(request.path, "id:test parent folder id")
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxApiService.createFolder(
                    CreateFolderRequest(
                        path = "/test parent folder/test folder"
                    )
                )
            }
        }

    @Test
    fun `given valid folder name and null parentId, when createFolder is success, then returns OmhFolder in root`() =
        runTest {
            coEvery {
                dropboxApiService.createFolder(
                    CreateFolderRequest(
                        path = "/test folder"
                    )
                )
            } returns Response.success(
                CreateFolderResponse(
                    TestFolderMetadata.testFolder.copy(
                        name = "test folder",
                        path = "/test folder"
                    )
                )
            )

            val result = fileRepositoryImpl.createFolder("test folder", "")
            assertNotNull(result)
            assertEquals("test folder", result?.name)

            coVerify(exactly = 0) { dropboxApiService.getNodeMetaData(any()) }
            coVerify(exactly = 1) {
                dropboxApiService.createFolder(
                    CreateFolderRequest(
                        path = "/test folder"
                    )
                )
            }
        }

    @Test
    fun `given createFolder fails, when called, then returns null`() =
        runTest {
            coEvery {
                dropboxApiService.createFolder(any())
            } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.createFolder("test folder", "")

            assertEquals(null, result)

            coVerify(exactly = 1) {
                dropboxApiService.createFolder(
                    CreateFolderRequest(path = "/test folder")
                )
            }
        }

    @Test
    fun `given valid file name and parentId, when createFile is success, then returns OmhFile`() =
        runTest {
            val parentFolderResponse = objectMapper.writerFor(FolderMetadata::class.java).writeValueAsString(
                TestFolderMetadata.testParentFolder
            ).toResponseBody("application/json".toMediaTypeOrNull())

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(parentFolderResponse)

            coEvery {
                dropboxContentApiService.createFile(any(), any())
            } returns Response.success(TestFileMetadata.testCreatedFileWithParent)

            val result = fileRepositoryImpl.createFile("test file.txt", "id:test parent folder id")

            assertNotNull(result)
            assertEquals("test file.txt", result?.name)
            assertEquals("newly created file with parent id", result?.id)

            coVerify(exactly = 1) {
                dropboxApiService.getNodeMetaData(
                    withArg { request ->
                        assertEquals("id:test parent folder id", request.path)
                    }
                )
            }
            coVerify(exactly = 1) {
                dropboxContentApiService.createFile(any(), any())
            }
        }

    @Test
    fun `given valid file name and null parentId, when createFile is success, then returns OmhFile in root`() =
        runTest {
            coEvery {
                dropboxContentApiService.createFile(any(), any())
            } returns Response.success(TestFileMetadata.testCreatedFile)

            val result = fileRepositoryImpl.createFile("test file.txt", "")

            assertNotNull(result)
            assertEquals("test file.txt", result?.name)
            assertEquals("newly created file id", result?.id)

            coVerify(exactly = 0) { dropboxApiService.getNodeMetaData(any()) }
            coVerify(exactly = 1) {
                dropboxContentApiService.createFile(any(), any())
            }
        }

    @Test
    fun `given createFile fails, when called, then returns null`() =
        runTest {
            coEvery {
                dropboxContentApiService.createFile(any(), any())
            } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.createFile("test file.txt", "")

            assertEquals(null, result)

            coVerify(exactly = 1) {
                dropboxContentApiService.createFile(any(), any())
            }
        }

    @Test
    fun `given valid fileId, when permanentlyDeleteFile is success, then returns true`() =
        runTest {
            coEvery {
                dropboxApiService.permanentlyDeleteFile(any())
            } returns Response.success(Unit)

            val result = fileRepositoryImpl.permanentlyDeleteFile("id:testFile1")
            assertTrue(result)
            coVerify {
                dropboxApiService.permanentlyDeleteFile(eq(PathRequestBody("id:testFile1")))
            }
        }

    @Test
    fun `given permanentlyDeleteFile fails, when called, then throws ApiException`() =
        runTest {
            coEvery { dropboxApiService.permanentlyDeleteFile(any()) } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.permanentlyDeleteFile("id:testFile1")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxApiService.permanentlyDeleteFile(eq(PathRequestBody("id:testFile1")))
            }
        }

    @Test
    fun `given valid fileId, when getFileRevisions is success, then returns list of OmhFileVersion`() =
        runTest {
            // Create a real ListFileRevisionsResponse with mock FileMetadata entries
            val mockFileMetadata = TestFileMetadata.testFile.copy(
                size = 1L,
                id = "file123",
                rev = "rev123"
            )

            val fileRevisionsResponse = ListFileRevisionsResponse(
                entries = listOf(mockFileMetadata),
                hasMore = false,
                isDeleted = false
            )

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                objectMapper.writerFor(FileMetadata::class.java)
                    .writeValueAsString(
                        TestFileMetadata.testFile
                    ).toResponseBody("application/json".toMediaTypeOrNull())
            )

            coEvery {
                dropboxApiService.getFileRevisionList(any())
            } returns Response.success(fileRevisionsResponse)

            val result = fileRepositoryImpl.getFileRevisions("id:testFile1")

            assertEquals(1, result.size)
            assertEquals("file123", result[0].fileId)
            assertEquals("rev123", result[0].versionId)
            coVerify {
                dropboxApiService.getFileRevisionList(
                    eq(GetFileRevisionsRequest(path = "/testfile.txt", mode = "path"))
                )
            }
        }

    @Test
    fun `given getFileRevisions fails, when called, then throws ApiException`() =
        runTest {
            coEvery {
                dropboxApiService.getFileRevisionList(any())
            } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                objectMapper.writerFor(FileMetadata::class.java).writeValueAsString(
                    TestFileMetadata.testFile
                ).toResponseBody("application/json".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.getFileRevisions("id:testFile1")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxApiService.getFileRevisionList(
                    eq(
                        GetFileRevisionsRequest(
                            path = "/testfile.txt",
                            mode = "path"
                        )
                    )
                )
            }
        }

    @Test
    fun `given valid fileId and versionId, when downloadFileVersion is success, then returns ByteArrayOutputStream`() =
        runTest {
            coEvery {
                dropboxContentApiService.downloadFile(any())
            } returns Response.success(
                "version file content".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.downloadFileVersion("id:testFile1", "version123")

            assertNotNull(result)
            coVerify {
                dropboxContentApiService.downloadFile(
                    eq(objectMapper.writeValueAsString(PathRequestBody("rev:version123")))
                )
            }
        }

    @Test
    fun `given downloadFileVersion fails, when called, then throws ApiException`() =
        runTest {
            coEvery {
                dropboxContentApiService.downloadFile(any())
            } returns Response.error(
                404,
                "Version not found".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.downloadFileVersion("id:testFile1", "invalidVersion")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxContentApiService.downloadFile(
                    eq(objectMapper.writeValueAsString(PathRequestBody("rev:invalidVersion")))
                )
            }
        }

    @Test
    fun `given valid fileId, when getFileMetadata is success, then returns OmhStorageMetadata for file`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.getFileMetadata("id:testFile1")

            assertNotNull(result)
            assertTrue(result?.entity is OmhStorageEntity.OmhFile)
            val file = result?.entity as OmhStorageEntity.OmhFile
            assertEquals("newly created file id", file.id)
            assertEquals("test file.txt", file.name)
            assertNotNull(result.originalMetadata)
            assertEquals("file", (result.originalMetadata as org.json.JSONObject).getString(".tag"))

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFile1"))
                )
            }
        }

    @Test
    fun `given valid folderId, when getFileMetadata is success, then returns OmhStorageMetadata for folder`() =
        runTest {
            val testFolderMetadataJson = objectMapper.writeValueAsString(TestFolderMetadata.testFolder)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.getFileMetadata("id:testFolder1")

            assertNotNull(result)
            assertTrue(result?.entity is OmhStorageEntity.OmhFolder)
            val folder = result?.entity as OmhStorageEntity.OmhFolder
            assertEquals("id:newly created folder id", folder.id)
            assertEquals("newly created folder name", folder.name)
            assertNotNull(result.originalMetadata)
            assertEquals("folder", (result.originalMetadata as org.json.JSONObject).getString(".tag"))

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFolder1"))
                )
            }
        }

    @Test
    fun `given valid nodeId and path, when getNodeMetadata is success, then returns NodeMetadata`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.getNodeMetadata(id = "id:testFileId", path = null)

            assertNotNull(result)
            assertTrue(result is FileMetadata)
            val fileMetadata = result as FileMetadata
            assertEquals("newly created file id", fileMetadata.id)
            assertEquals("test file.txt", fileMetadata.name)
            assertEquals("/test file.txt", fileMetadata.path)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFileId"))
                )
            }
        }

    @Test
    fun `given valid path only, when getNodeMetadata is success, then returns NodeMetadata`() =
        runTest {
            val testFolderMetadataJson = objectMapper.writeValueAsString(TestFolderMetadata.testFolder)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.getNodeMetadata(id = null, path = "/test/folder/path")

            assertNotNull(result)
            assertTrue(result is FolderMetadata)
            val folderMetadata = result as FolderMetadata
            assertEquals("id:newly created folder id", folderMetadata.id)
            assertEquals("newly created folder name", folderMetadata.name)
            assertEquals("/new folder", folderMetadata.path)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("/test/folder/path"))
                )
            }
        }

    @Test
    fun `given getNodeMetadata returns null, when called, then returns null`() =
        runTest {
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(null)

            val result = fileRepositoryImpl.getNodeMetadata(id = "id:testFileId", path = null)

            assertEquals(null, result)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFileId"))
                )
            }
        }

    @Test
    fun `given valid query, when search is success, then returns SearchResultResponse`() =
        runTest {
            val searchResponseJson = JSONObject().apply {
                put("has_more", false)
                put("cursor", "")
                put(
                    "matches",
                    org.json.JSONArray().apply {
                        // File match
                        put(
                            JSONObject().apply {
                                put("match_type", JSONObject().apply { put(".tag", "filename") })
                                put(
                                    "metadata",
                                    JSONObject().apply {
                                        put(".tag", "metadata")
                                        put(
                                            "metadata",
                                            JSONObject().apply {
                                                put(".tag", "file")
                                                put("id", "search_file_1")
                                                put("name", "search_result.txt")
                                                put("path_display", "/search_result.txt")
                                                put("path_lower", "/search_result.txt")
                                                put("size", 1024)
                                                put("server_modified", "2023-10-01T12:00:00Z")
                                                put("client_modified", "2023-10-01T12:00:00Z")
                                                put("rev", "search_rev")
                                                put("is_downloadable", true)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                        // Folder match
                        put(
                            JSONObject().apply {
                                put("match_type", JSONObject().apply { put(".tag", "filename") })
                                put(
                                    "metadata",
                                    JSONObject().apply {
                                        put(".tag", "metadata")
                                        put(
                                            "metadata",
                                            JSONObject().apply {
                                                put(".tag", "folder")
                                                put("id", "search_folder_1")
                                                put("name", "search_folder")
                                                put("path_display", "/search_folder")
                                                put("path_lower", "/search_folder")
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            coEvery {
                dropboxApiService.search(any())
            } returns Response.success(
                searchResponseJson.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.search("test query")

            assertNotNull(result)
            assertEquals(2, result.matches.size)
            assertTrue(result.matches[0] is FileMetadata)
            assertTrue(result.matches[1] is FolderMetadata)

            val fileMetadata = result.matches[0] as FileMetadata
            assertEquals("search_file_1", fileMetadata.id)
            assertEquals("search_result.txt", fileMetadata.name)

            val folderMetadata = result.matches[1] as FolderMetadata
            assertEquals("search_folder_1", folderMetadata.id)
            assertEquals("search_folder", folderMetadata.name)

            coVerify {
                dropboxApiService.search(
                    eq(SearchFileRequest("test query"))
                )
            }
        }

    @Test
    fun `given search with pagination, when search is called, handles multiple pages correctly`() =
        runTest {
            // First page response with has_more = true
            val firstPageResponse = JSONObject().apply {
                put("has_more", true)
                put("cursor", "next_page_cursor")
                put(
                    "matches",
                    org.json.JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("match_type", JSONObject().apply { put(".tag", "filename") })
                                put(
                                    "metadata",
                                    JSONObject().apply {
                                        put(".tag", "metadata")
                                        put(
                                            "metadata",
                                            JSONObject().apply {
                                                put(".tag", "file")
                                                put("id", "page1_file")
                                                put("name", "page1_file.txt")
                                                put("path_display", "/page1_file.txt")
                                                put("path_lower", "/page1_file.txt")
                                                put("size", 512)
                                                put("server_modified", "2023-10-01T12:00:00Z")
                                                put("client_modified", "2023-10-01T12:00:00Z")
                                                put("rev", "page1_rev")
                                                put("is_downloadable", true)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            // Second page response with has_more = false
            val secondPageResponse = JSONObject().apply {
                put("has_more", false)
                put("cursor", "")
                put(
                    "matches",
                    org.json.JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("match_type", JSONObject().apply { put(".tag", "filename") })
                                put(
                                    "metadata",
                                    JSONObject().apply {
                                        put(".tag", "metadata")
                                        put(
                                            "metadata",
                                            JSONObject().apply {
                                                put(".tag", "file")
                                                put("id", "page2_file")
                                                put("name", "page2_file.txt")
                                                put("path_display", "/page2_file.txt")
                                                put("path_lower", "/page2_file.txt")
                                                put("size", 256)
                                                put("server_modified", "2023-10-01T12:00:00Z")
                                                put("client_modified", "2023-10-01T12:00:00Z")
                                                put("rev", "page2_rev")
                                                put("is_downloadable", true)
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
            }

            coEvery {
                dropboxApiService.search(any<SearchFileRequest>())
            } returns Response.success(
                firstPageResponse.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )

            coEvery {
                dropboxApiService.continueSearch(any())
            } returns Response.success(
                secondPageResponse.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.search("test pagination")

            assertNotNull(result)
            assertEquals(2, result.matches.size)

            // Verify first page file
            val firstFile = result.matches[0] as FileMetadata
            assertEquals("page1_file", firstFile.id)
            assertEquals("page1_file.txt", firstFile.name)

            // Verify second page file
            val secondFile = result.matches[1] as FileMetadata
            assertEquals("page2_file", secondFile.id)
            assertEquals("page2_file.txt", secondFile.name)

            coVerify {
                dropboxApiService.search(
                    eq(SearchFileRequest("test pagination"))
                )
            }
            coVerify {
                dropboxApiService.continueSearch(
                    eq(SearchFileRequest.SearchByCursor("next_page_cursor"))
                )
            }
        }

    @Test
    fun `given search fails, when called, then throws ApiException`() =
        runTest {
            coEvery {
                dropboxApiService.search(any())
            } returns Response.error(
                500,
                "Search service unavailable".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.search("test query")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxApiService.search(
                    eq(
                        com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.SearchFileRequest(
                            "test query"
                        )
                    )
                )
            }
        }

    @Test
    fun `given invalid search response, when search is called, then throws ApiException`() =
        runTest {
            // Response without "matches" field
            val invalidResponseJson = JSONObject().apply {
                put("has_more", false)
                put("cursor", "")
                // Missing "matches" field
            }

            coEvery {
                dropboxApiService.search(any())
            } returns Response.success(
                invalidResponseJson.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.search("test query")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(400, expected.statusCode)
                assertEquals("Invalid search response", expected.message)
            }

            coVerify {
                dropboxApiService.search(
                    eq(SearchFileRequest("test query"))
                )
            }
        }

    @Test
    fun `given valid fileId, list of OmhPermission for file`() =
        runTest {
            // Mock getNodeMetadata to return FileMetadata
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val listFileSharedMembersResponse = testFileSharedMembersResponseJson
                .toString()

            // Mock file shared members API call
            coEvery {
                dropboxApiService.listFileSharedMembers(any())
            } returns Response.success(
                listFileSharedMembersResponse
                    .toResponseBody("application/json".toMediaTypeOrNull())
            )

            coEvery {
                dropboxApiService.continueListFileSharedMembers(any())
            } returns Response.success("{}".toResponseBody())

            val result = fileRepositoryImpl.getNodePermission("id:testFile1")

            assertEquals(testExpectedPermissions, result)
            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFile1"))
                )
            }
            coVerify {
                dropboxApiService.listFileSharedMembers(
                    eq(ListFileSharedMembersRequest("id:testFile1"))
                )
            }
        }

    @Test
    fun `given valid folderId, returns list of OmhPermission for folder`() =
        runTest {
            // Mock getNodeMetadata to return FolderMetadata
            val testFolderMetadataJson =
                objectMapper.writeValueAsString(TestFolderMetadata.testFolder)
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val listFolderSharedMemberResponse: ResponseBody =
                testFolderSharedMembersResponseJson.toString().toResponseBody(
                    "application/json".toMediaTypeOrNull()
                )

            // Mock folder shared members API call
            coEvery {
                dropboxApiService.listFolderSharedMembers(any())
            } returns Response.success(
                listFolderSharedMemberResponse
            )

            coEvery {
                dropboxApiService.continueListFolderSharedMembers(any())
            } returns Response.success("{}".toResponseBody())

            val result = fileRepositoryImpl.getNodePermission("id:testFolder1")

            assertEquals(testExpectedPermissions, result)
            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:testFolder1"))
                )
            }
            coVerify {
                dropboxApiService.listFolderSharedMembers(
                    eq(ListFolderSharedMembersRequest("shared folder id"))
                )
            }
        }

    @Test
    fun `given node not found, when getNodePermission is called, then throws ApiException`() =
        runTest {
            // Mock getNodeMetadata to return null (node not found)
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(null)

            try {
                fileRepositoryImpl.getNodePermission("id:nonExistentNode")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(404, expected.statusCode)
                assertEquals("Node not found", expected.message)
            }

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(NodeMetadataRequest("id:nonExistentNode"))
                )
            }
            // Should not call shared members APIs when node is not found
            coVerify(exactly = 0) {
                dropboxApiService.listFileSharedMembers(any())
            }
            coVerify(exactly = 0) {
                dropboxApiService.listFolderSharedMembers(any())
            }
        }

    @Test
    fun `if node not found, when createNodePermission is called, then throws ApiException with`() =
        runTest {
            // Arrange: node metadata returns null
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(null)

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.READER,
                recipient = OmhPermissionRecipient.User(TEST_PERMISSION_USER_EMAIL)
            )

            // Act + Assert
            try {
                fileRepositoryImpl.createNodePermission("id:nonExistentNode", create)
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(404, expected.statusCode)
                assertEquals("Node not found", expected.message)
            }

            // Verify
            coVerify {
                dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:nonExistentNode")))
            }
            coVerify(exactly = 0) { dropboxApiService.addFileSharedMember(any()) }
            coVerify(exactly = 0) { dropboxApiService.addFolderSharedMember(any()) }
        }

    @Test
    fun `given valid fileId and permission, when createNodePermission is success for file`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            // Will be called twice: once by createNodePermission and once by getNodePermission
            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            // add file member succeeds
            coEvery { dropboxApiService.addFileSharedMember(any()) } returns Response.success("{}".toResponseBody())

            // list members returns the user/group set including our created user
            coEvery { dropboxApiService.listFileSharedMembers(any()) } returns Response.success(
                testFileSharedMembersResponseJson.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFileSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.READER,
                recipient = OmhPermissionRecipient.User(TEST_PERMISSION_USER_EMAIL)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFile1", create)

            assertEquals(testOmhUserPermission, result)
            coVerify {
                dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:testFile1")))
            }
            coVerify {
                dropboxApiService.addFileSharedMember(
                    withArg { req ->
                        assertEquals("id:testFile1", req.fileId)
                        assertEquals(1, req.members.size)
                        assertEquals("email", req.members.first().tag)
                        assertEquals(TEST_PERMISSION_USER_EMAIL, req.members.first().email)
                        assertEquals(OmhPermissionRole.READER, req.accessLevel)
                    }
                )
            }
            coVerify {
                dropboxApiService.listFileSharedMembers(eq(ListFileSharedMembersRequest("id:testFile1")))
            }
        }

    @Test
    fun `given valid folderId and permission, when createNodePermission is success for folder`() =
        runTest {
            // Use folder with existing sharedFolderId to avoid sharing flow
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "existing_shared_folder_id")
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithSharing)
            // Will be called twice
            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            // add folder member succeeds
            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.success("{}".toResponseBody())

            // list members returns the user/group set including our created group
            // Mock permission listing for final verification - should include the newly created group permission
            val folderSharedMembersWithNewGroup = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "editor")
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                                put("is_inherited", false)
                            }
                        )
                    }
                )
                put("cursor", "")
                put("has_more", false)
            }
            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                folderSharedMembersWithNewGroup.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFolder1", create)

            assertEquals(testOmhGroupPermission, result)
            coVerify {
                dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:testFolder1")))
            }
            coVerify {
                dropboxApiService.addFolderSharedMember(
                    withArg { req ->
                        assertEquals("existing_shared_folder_id", req.sharedFolderId)
                        assertEquals(1, req.members.size)
                        assertEquals("dropbox_id", req.members.first().member.tag)
                        assertEquals(TEST_PERMISSION_GROUP_ID, req.members.first().member.userId)
                        assertEquals(OmhPermissionRole.WRITER, req.members.first().accessLevel)
                    }
                )
            }
            coVerify {
                dropboxApiService.listFolderSharedMembers(
                    eq(ListFolderSharedMembersRequest("existing_shared_folder_id"))
                )
            }
        }

    @Test
    fun `given createNodePermission fails for file, when called, then throws ApiException`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.addFileSharedMember(any()) } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.createNodePermission(
                    "id:testFile1",
                    OmhCreatePermission.CreateIdentityPermission(
                        role = OmhPermissionRole.READER,
                        recipient = OmhPermissionRecipient.User(TEST_PERMISSION_USER_EMAIL)
                    )
                )
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify { dropboxApiService.addFileSharedMember(any()) }
        }

    @Test
    fun `given createNodePermission fails for folder, when called, then throws ApiException`() =
        runTest {
            // Use folder with existing sharedFolderId to avoid sharing flow
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "existing_shared_folder_id")
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithSharing)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.createNodePermission(
                    "id:testFolder1",
                    OmhCreatePermission.CreateIdentityPermission(
                        role = OmhPermissionRole.WRITER,
                        recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
                    )
                )
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify { dropboxApiService.addFolderSharedMember(any()) }
        }

    @Test
    fun `given valid fileId and permissionId, updateNodePermission success for file`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )
            coEvery { dropboxApiService.updateFileSharedMember(any()) } returns Response.success("{}".toResponseBody())

            // Create updated response with WRITER role instead of READER
            val updatedFileSharedMembersResponse = JSONObject().apply {
                put(
                    "users",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put(
                                    "access_type",
                                    JSONObject().apply { put(".tag", "editor") } // WRITER role
                                )
                                put("is_inherited", false)
                                put(
                                    "user",
                                    JSONObject().apply {
                                        put("account_id", TEST_PERMISSION_USER_ID)
                                        put("display_name", TEST_PERMISSION_USER_NAME)
                                        put("email", TEST_PERMISSION_USER_EMAIL)
                                    }
                                )
                            }
                        )
                    }
                )
                put("groups", JSONArray())
            }

            coEvery { dropboxApiService.listFileSharedMembers(any()) } returns Response.success(
                updatedFileSharedMembersResponse.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFileSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val result = fileRepositoryImpl.updateNodePermission(
                "id:testFile1",
                TEST_PERMISSION_USER_ID,
                OmhPermissionRole.WRITER
            )

            assertEquals(testOmhUserPermission.copy(role = OmhPermissionRole.WRITER), result)
            coVerify {
                dropboxApiService.updateFileSharedMember(
                    withArg { req ->
                        assertEquals("id:testFile1", req.fileId)
                        assertEquals(OmhPermissionRole.WRITER, req.accessLevel)
                        assertEquals("dropbox_id", req.member.tag)
                        assertEquals(TEST_PERMISSION_USER_ID, req.member.userId)
                    }
                )
            }
        }

    @Test
    fun `given valid folderId and permissionId, updateNodePermission success for folder`() =
        runTest {
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "existing_shared_folder_id")
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithSharing)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )
            coEvery { dropboxApiService.updateFolderSharedMember(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            // Create updated response with READER role instead of WRITER for group
            val updatedFolderSharedMembersResponse = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "viewer_no_comment") // READER role
                                put("is_inherited", false)
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                            }
                        )
                    }
                )
            }

            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                updatedFolderSharedMembersResponse.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val result = fileRepositoryImpl.updateNodePermission(
                "id:testFolder1",
                TEST_PERMISSION_GROUP_ID,
                OmhPermissionRole.READER
            )

            assertEquals(testOmhGroupPermission.copy(role = OmhPermissionRole.READER), result)
            coVerify {
                dropboxApiService.updateFolderSharedMember(
                    withArg { req ->
                        assertEquals("existing_shared_folder_id", req.sharedFolderId)
                        assertEquals(OmhPermissionRole.READER, req.accessLevel)
                        assertEquals("dropbox_id", req.member.tag)
                        assertEquals(TEST_PERMISSION_GROUP_ID, req.member.userId)
                    }
                )
            }
        }

    @Test
    fun `given updateNodePermission fails for file, when called, then throws ApiException`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.updateFileSharedMember(any()) } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.updateNodePermission(
                    "id:testFile1",
                    TEST_PERMISSION_USER_ID,
                    OmhPermissionRole.WRITER
                )
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify { dropboxApiService.updateFileSharedMember(any()) }
        }

    @Test
    fun `given updateNodePermission fails for folder, when called, then throws ApiException`() =
        runTest {
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "existing_shared_folder_id")
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithSharing)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.updateFolderSharedMember(any()) } returns Response.error(
                500,
                "Server error".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.updateNodePermission(
                    "id:testFolder1",
                    TEST_PERMISSION_GROUP_ID,
                    OmhPermissionRole.READER
                )
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify { dropboxApiService.updateFolderSharedMember(any()) }
        }

    @Test
    fun `given node not found, when updateNodePermission is called, then throws ApiException with 404`() =
        runTest {
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(null)

            try {
                fileRepositoryImpl.updateNodePermission("id:nonExistentNode", "anyPermId", OmhPermissionRole.WRITER)
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(404, expected.statusCode)
                assertEquals("Node not found", expected.message)
            }

            coVerify { dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:nonExistentNode"))) }
            coVerify(exactly = 0) { dropboxApiService.updateFileSharedMember(any()) }
            coVerify(exactly = 0) { dropboxApiService.updateFolderSharedMember(any()) }
        }

    @Test
    fun `given valid fileId and permissionId, return true when deleteNodePermission success`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.deleteFileSharedMembers(any()) } returns Response.success(Unit)

            val result = fileRepositoryImpl.deleteNodePermission("id:testFile1", "user123")

            assertTrue(result)
            coVerify { dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:testFile1"))) }
            coVerify {
                dropboxApiService.deleteFileSharedMembers(
                    eq(DeleteFileSharedMemberRequest(fileId = "id:testFile1", memberId = "user123"))
                )
            }
        }

    @Test
    fun `given valid folderId and permissionId, return true when deleteNodePermission success`() =
        runTest {
            val testFolderMetadataJson = objectMapper.writeValueAsString(TestFolderMetadata.testFolder)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.deleteFolderSharedMembers(any()) } returns Response.success(Unit)

            val result = fileRepositoryImpl.deleteNodePermission("id:testFolder1", "group456")

            assertTrue(result)
            coVerify { dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:testFolder1"))) }
            coVerify {
                dropboxApiService.deleteFolderSharedMembers(
                    eq(
                        DeleteFolderSharedMemberRequest(
                            sharedFolderId = "shared folder id",
                            memberId = "group456"
                        )
                    )
                )
            }
        }

    @Test
    fun `given node not found, when deleteNodePermission is called, then throws ApiException`() =
        runTest {
            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(null)

            try {
                fileRepositoryImpl.deleteNodePermission("id:nonExistentNode", "anyPermId")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(404, expected.statusCode)
                assertEquals("Node not found", expected.message)
            }

            coVerify { dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:nonExistentNode"))) }
            coVerify(exactly = 0) { dropboxApiService.deleteFileSharedMembers(any()) }
            coVerify(exactly = 0) { dropboxApiService.deleteFolderSharedMembers(any()) }
        }

    @Test
    fun `given valid nodeId, when getNodeMetadataRaw is success, then returns JSON string`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(
                TestFileMetadata.testCreatedFile
            )

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            // Use reflection to access the private getNodeMetadataRaw method
            val method = DropboxRestfulFileRepository::class.memberFunctions.find {
                it.name == "getNodeMetadataRaw"
            }.also { it?.isAccessible = true }

            val result = method?.callSuspend(fileRepositoryImpl, "id:testFileId", null) as String?

            assertNotNull(result)
            assertEquals(testFileMetadataJson, result)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(
                        NodeMetadataRequest(
                            "id:testFileId"
                        )
                    )
                )
            }
        }

    @Test
    fun `given valid path, when getNodeMetadataRaw is success, then returns JSON string`() =
        runTest {
            val testFolderMetadataJson = objectMapper.writeValueAsString(
                TestFolderMetadata.testFolder
            )

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            // Use reflection to access the private getNodeMetadataRaw method
            val method = DropboxRestfulFileRepository::class.memberFunctions.find {
                it.name == "getNodeMetadataRaw"
            }.also { it?.isAccessible = true }

            val result = method?.callSuspend(fileRepositoryImpl, null, "/test/folder/path") as String?

            assertNotNull(result)
            assertEquals(testFolderMetadataJson, result)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(
                        NodeMetadataRequest(
                            "/test/folder/path"
                        )
                    )
                )
            }
        }

    @Test
    fun `given getNodeMetadataRaw returns null response body, when called, then returns null`() =
        runTest {
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(null)

            // Use reflection to access the private getNodeMetadataRaw method
            val method = DropboxRestfulFileRepository::class.memberFunctions.find {
                it.name == "getNodeMetadataRaw"
            }.also { it?.isAccessible = true }

            val result = method?.callSuspend(fileRepositoryImpl, "id:testFileId", null) as String?

            assertEquals(null, result)

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(
                        NodeMetadataRequest(
                            "id:testFileId"
                        )
                    )
                )
            }
        }

    @Test
    fun `given file JSON, when jsonToNodeMetadata is called, then returns FileMetadata`() =
        runTest {
            // Create a valid file JSON
            val fileJson = org.json.JSONObject().apply {
                put(".tag", "file")
                put("id", "test_file_id")
                put("name", "test_file.txt")
                put("path_lower", "/test_file.txt")
                put("path_display", "/test_file.txt")
                put("size", 1024)
                put("server_modified", "2023-10-01T12:00:00Z")
                put("client_modified", "2023-10-01T12:00:00Z")
                put("rev", "test_rev")
                put("is_downloadable", true)
            }

            // Use reflection to access the private jsonToNodeMetadata method
            val method = DropboxRestfulFileRepository::class.java.getDeclaredMethod(
                "jsonToNodeMetadata",
                JSONObject::class.java
            )
            method.isAccessible = true

            val result = method.invoke(fileRepositoryImpl, fileJson)

            assertTrue(
                result is FileMetadata
            )
            val fileMetadata = result as FileMetadata
            assertEquals("test_file_id", fileMetadata.id)
            assertEquals("test_file.txt", fileMetadata.name)
            assertEquals("/test_file.txt", fileMetadata.path)
        }

    @Test
    fun `given folder JSON, when jsonToNodeMetadata is called, then returns FolderMetadata`() =
        runTest {
            // Create a valid folder JSON
            val folderJson = JSONObject().apply {
                put(".tag", "folder")
                put("id", "test_folder_id")
                put("name", "test_folder")
                put("path_lower", "/test_folder")
                put("path_display", "/test_folder")
            }

            // Use reflection to access the private jsonToNodeMetadata method
            val method = DropboxRestfulFileRepository::class.java.getDeclaredMethod(
                "jsonToNodeMetadata",
                JSONObject::class.java
            )
            method.isAccessible = true

            val result = method.invoke(fileRepositoryImpl, folderJson)

            assertTrue(
                result is FolderMetadata
            )
            val folderMetadata = result as FolderMetadata
            assertEquals("test_folder_id", folderMetadata.id)
            assertEquals("test_folder", folderMetadata.name)
            assertEquals("/test_folder", folderMetadata.path)
        }

    @Test
    fun `given valid fileId and exportedMimeType, when exportFile is success, then returns ByteArrayOutputStream`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedPaper)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            coEvery {
                dropboxContentApiService.exportFile(any())
            } returns Response.success(
                "exported file content".toResponseBody("text/html".toMediaTypeOrNull())
            )

            val result = fileRepositoryImpl.exportFile("id:testFile1", "html")

            assertNotNull(result)
            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(
                        NodeMetadataRequest(
                            "id:testFile1"
                        )
                    )
                )
            }
            coVerify {
                dropboxContentApiService.exportFile(
                    match {
                        val obj = objectMapper.readValue(it, ExportFileRequest::class.java)
                        obj.path == "id:testFile1" && obj.exportFormat == "html"
                    }
                )
            }
        }

    @Test
    fun `given exportFile API call fails, when exportFile is called, then throws ApiException`() =
        runTest {
            val testFileMetadataJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)

            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(
                testFileMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            coEvery {
                dropboxContentApiService.exportFile(any())
            } returns Response.error(
                500,
                "Export failed".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            try {
                fileRepositoryImpl.exportFile("id:testFile1", "application/pdf")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {}

            coVerify {
                dropboxApiService.getNodeMetaData(any())
            }
            coVerify {
                dropboxContentApiService.exportFile(any())
            }
        }

    @Test
    fun `given file not found, when exportFile is called, then throws ApiException with 404`() =
        runTest {
            coEvery {
                dropboxApiService.getNodeMetaData(any())
            } returns Response.success(null)

            try {
                fileRepositoryImpl.exportFile("id:nonExistentFile", "application/pdf")
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(404, expected.statusCode)
                assertEquals("File not found", expected.message)
            }

            coVerify {
                dropboxApiService.getNodeMetaData(
                    eq(
                        NodeMetadataRequest(
                            "id:nonExistentFile"
                        )
                    )
                )
            }
            coVerify(exactly = 0) {
                dropboxContentApiService.exportFile(any())
            }
        }

    @Test
    fun `given valid fileId, when getTemporaryLink is called then returns preview url`() = runTest {
        val fileJson = objectMapper.writeValueAsString(TestFileMetadata.testCreatedFile)
        coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
            fileJson.toResponseBody("application/json".toMediaTypeOrNull())
        )
        val sharedFile = SharedFileMetadata(
            id = "newly created file id",
            name = "test file.txt",
            previewUrl = "https://preview/file",
            path = "/test file.txt"
        )
        coEvery { dropboxApiService.getFileSharingMetadata(any()) } returns Response.success(sharedFile)

        val result = fileRepositoryImpl.getTemporaryLink("id:testFile1")

        assertEquals("https://preview/file", result)
        coVerify { dropboxApiService.getFileSharingMetadata(match { it.file == "newly created file id" }) }
    }

    @Test
    fun `given folderId with sharing info when getTemporaryLink is called then returns preview url`() = runTest {
        val folderWithSharing = TestFolderMetadata.testFolder.copy(
            sharingInfo = FolderSharingInfo(sharedFolderId = "sfid123")
        )
        val folderJson = objectMapper.writeValueAsString(folderWithSharing)
        coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
            folderJson.toResponseBody("application/json".toMediaTypeOrNull())
        )
        val sharedFolder = SharedFolderMetadata(
            id = folderWithSharing.id,
            name = folderWithSharing.name,
            previewUrl = "https://preview/folder",
            path = folderWithSharing.path,
            sharedFolderId = "sfid123"
        )
        coEvery { dropboxApiService.getFolderSharingMetadata(any()) } returns Response.success(sharedFolder)

        val result = fileRepositoryImpl.getTemporaryLink("id:newly created folder id")

        assertEquals("https://preview/folder", result)
        coVerify { dropboxApiService.getFolderSharingMetadata(match { it.sharedFolderId == "sfid123" }) }
    }

    @Test
    fun `given folderId without sharing info when getTemporaryLink is called then returns null`() = runTest {
        val folderJson = objectMapper.writeValueAsString(TestFolderMetadata.testFolder)
        coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
            folderJson.toResponseBody("application/json".toMediaTypeOrNull())
        )

        val result = fileRepositoryImpl.getTemporaryLink("id:newly created folder id")

        assertEquals(null, result)
        coVerify(exactly = 0) { dropboxApiService.getFolderSharingMetadata(any()) }
    }

    @Test
    fun `given node not found when getTemporaryLink is called then returns null`() = runTest {
        coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(null)

        val result = fileRepositoryImpl.getTemporaryLink("id:missing")

        assertEquals(null, result)
        coVerify { dropboxApiService.getNodeMetaData(eq(NodeMetadataRequest("id:missing"))) }
    }

    // Tests for share job polling behavior

    @Test
    fun `folder without sharedFolderId, createNodePermission polls job status successfully after retry`() =
        runTest {
            // Create test folder without sharedFolderId (needs to be shared first)
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            // After sharing job completes, the folder should have a sharedFolderId
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "sf_123")
            val testFolderWithSharingJson = objectMapper.writeValueAsString(testFolderWithSharing)

            // Mock getNodeMetadata calls (first without sharing, then with sharing after job completes)
            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderWithSharingJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            // Mock shareFolder to return a job
            val shareJobResponse = ShareJobResponse(jobId = "test_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // Mock job status polling - first two calls return in_progress, third returns complete
            val inProgressResponseContent = JSONObject().apply {
                put(".tag", "in_progress")
            }.toString()

            val completedResponseContent = JSONObject().apply {
                put(".tag", "complete")
                put("folder_id", "sf_123")
                put("shared_folder_id", "sf_123")
                put("name", "test folder")
                put("preview_url", "https://preview.url")
            }.toString()

            coEvery { dropboxApiService.checkShareJobStatus(any()) } returnsMany listOf(
                Response.success(createReusableResponseBody(inProgressResponseContent)),
                Response.success(createReusableResponseBody(inProgressResponseContent)),
                Response.success(createReusableResponseBody(completedResponseContent))
            )

            // Mock addFolderSharedMember success
            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.success("{}".toResponseBody())

            // Mock permission listing for final verification - should include the newly created group permission
            val folderSharedMembersWithNewGroup = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "editor")
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                                put("is_inherited", false)
                            }
                        )
                    }
                )
                put("cursor", "")
                put("has_more", false)
            }
            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                folderSharedMembersWithNewGroup.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFolder1", create)

            assertEquals(testOmhGroupPermission, result)

            // Verify the sequence of calls
            coVerify { dropboxApiService.shareFolder(eq(ShareFolderRequestBody("id:testFolder1", true))) }
            coVerify(exactly = 3) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("test_job_123"))
                )
            }
            coVerify {
                dropboxApiService.addFolderSharedMember(
                    withArg { req ->
                        assertEquals("sf_123", req.sharedFolderId) // Should use the returned sharedFolderId
                        assertEquals(1, req.members.size)
                        assertEquals("dropbox_id", req.members.first().member.tag)
                        assertEquals(TEST_PERMISSION_GROUP_ID, req.members.first().member.userId)
                        assertEquals(OmhPermissionRole.WRITER, req.members.first().accessLevel)
                    }
                )
            }
        }

    @Test
    fun `given folder sharing job completes immediately, when createNodePermission called, then no polling required`() =
        runTest {
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            // After sharing job completes, the folder should have a sharedFolderId
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "sf_456")
            val testFolderWithSharingJson = objectMapper.writeValueAsString(testFolderWithSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderWithSharingJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            val shareJobResponse = ShareJobResponse(jobId = "immediate_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // Job completes immediately on first check
            val completedResponseContent = JSONObject().apply {
                put(".tag", "complete")
                put("folder_id", "sf_456")
                put("shared_folder_id", "sf_456")
                put("name", "test folder")
                put("preview_url", "https://preview.url")
            }.toString()

            coEvery { dropboxApiService.checkShareJobStatus(any()) } returns Response.success(
                createReusableResponseBody(completedResponseContent)
            )

            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.success("{}".toResponseBody())

            // Mock permission listing for final verification - should include the newly created group permission
            val folderSharedMembersWithNewGroup = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "editor")
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                                put("is_inherited", false)
                            }
                        )
                    }
                )
                put("cursor", "")
                put("has_more", false)
            }
            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                folderSharedMembersWithNewGroup.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFolder1", create)

            assertEquals(testOmhGroupPermission, result)

            // Verify only one job status check was needed
            coVerify(exactly = 1) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("immediate_job_123"))
                )
            }
        }

    @Test
    fun `given folder sharing job fails, when createNodePermission called, then throws ApiException`() =
        runTest {
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val shareJobResponse = ShareJobResponse(jobId = "failed_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // Job fails after being in progress
            val inProgressResponseContent = JSONObject().apply {
                put(".tag", "in_progress")
            }.toString()

            val failedResponseContent = JSONObject().apply {
                put(".tag", "failed")
                put("error", "Sharing failed due to permissions")
            }.toString()

            coEvery { dropboxApiService.checkShareJobStatus(any()) } returnsMany listOf(
                Response.success(createReusableResponseBody(inProgressResponseContent)),
                Response.success(createReusableResponseBody(failedResponseContent))
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            try {
                fileRepositoryImpl.createNodePermission("id:testFolder1", create)
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(500, expected.statusCode)
                assertTrue(expected.message!!.contains("Share job failed"))
                assertTrue(expected.message!!.contains("Sharing failed due to permissions"))
            }

            coVerify { dropboxApiService.shareFolder(eq(ShareFolderRequestBody("id:testFolder1", true))) }
            coVerify(exactly = 2) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("failed_job_123"))
                )
            }
            // Should not attempt to add folder member if sharing failed
            coVerify(exactly = 0) { dropboxApiService.addFolderSharedMember(any()) }
        }

    @Test
    fun `given folder sharing job times out, when createNodePermission called, then throws ApiException`() =
        runTest {
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val shareJobResponse = ShareJobResponse(jobId = "timeout_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // Job always returns in_progress (simulating timeout)
            val inProgressResponseContent = JSONObject().apply {
                put(".tag", "in_progress")
            }.toString()

            coEvery { dropboxApiService.checkShareJobStatus(any()) } returns Response.success(
                createReusableResponseBody(inProgressResponseContent)
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            try {
                fileRepositoryImpl.createNodePermission("id:testFolder1", create)
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(408, expected.statusCode)
                assertTrue(expected.message!!.contains("Share job timed out"))
            }

            coVerify { dropboxApiService.shareFolder(eq(ShareFolderRequestBody("id:testFolder1", true))) }
            // Should make maximum number of attempts
            coVerify(exactly = 30) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("timeout_job_123"))
                )
            }
            coVerify(exactly = 0) { dropboxApiService.addFolderSharedMember(any()) }
        }

    @Test
    fun `given folder sharing job status API fails, when createNodePermission called, then throws ApiException`() =
        runTest {
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returns Response.success(
                testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())
            )

            val shareJobResponse = ShareJobResponse(jobId = "api_fail_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // Job status API call fails
            coEvery { dropboxApiService.checkShareJobStatus(any()) } returns Response.error(
                500,
                "Job status API unavailable".toResponseBody("text/plain".toMediaTypeOrNull())
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            try {
                fileRepositoryImpl.createNodePermission("id:testFolder1", create)
                fail("Expected ApiException to be thrown")
            } catch (expected: OmhStorageException.ApiException) {
                assertEquals(500, expected.statusCode)
            }

            coVerify { dropboxApiService.shareFolder(eq(ShareFolderRequestBody("id:testFolder1", true))) }
            coVerify(exactly = 1) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("api_fail_job_123"))
                )
            }
            coVerify(exactly = 0) { dropboxApiService.addFolderSharedMember(any()) }
        }

    @Test
    fun `given folder with existing sharedFolderId, when createNodePermission called, then no sharing job needed`() =
        runTest {
            // Create test folder WITH existing sharedFolderId
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "existing_sf_123")
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.success("{}".toResponseBody())
            // Mock permission listing for final verification - should include the newly created group permission
            val folderSharedMembersWithNewGroup = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "editor")
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                                put("is_inherited", false)
                            }
                        )
                    }
                )
                put("cursor", "")
                put("has_more", false)
            }
            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                folderSharedMembersWithNewGroup.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFolder1", create)

            assertEquals(testOmhGroupPermission, result)

            // Verify no sharing job was initiated since folder already has sharedFolderId
            coVerify(exactly = 0) { dropboxApiService.shareFolder(any()) }
            coVerify(exactly = 0) { dropboxApiService.checkShareJobStatus(any()) }
            coVerify {
                dropboxApiService.addFolderSharedMember(
                    withArg { req ->
                        assertEquals("existing_sf_123", req.sharedFolderId) // Should use existing sharedFolderId
                        assertEquals(1, req.members.size)
                        assertEquals("dropbox_id", req.members.first().member.tag)
                        assertEquals(TEST_PERMISSION_GROUP_ID, req.members.first().member.userId)
                        assertEquals(OmhPermissionRole.WRITER, req.members.first().accessLevel)
                    }
                )
            }
        }

    @Test
    fun `given folder sharing job returns unknown status, when createNodePermission called, then continues polling`() =
        runTest {
            val testFolderWithoutSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = null)
            val testFolderMetadataJson = objectMapper.writeValueAsString(testFolderWithoutSharing)

            // After sharing job completes, the folder should have a sharedFolderId
            val testFolderWithSharing = TestFolderMetadata.testFolder.copy(sharedFolderId = "sf_789")
            val testFolderWithSharingJson = objectMapper.writeValueAsString(testFolderWithSharing)

            coEvery { dropboxApiService.getNodeMetaData(any()) } returnsMany listOf(
                Response.success(testFolderMetadataJson.toResponseBody("application/json".toMediaTypeOrNull())),
                Response.success(testFolderWithSharingJson.toResponseBody("application/json".toMediaTypeOrNull()))
            )

            val shareJobResponse = ShareJobResponse(jobId = "unknown_status_job_123")
            coEvery { dropboxApiService.shareFolder(any()) } returns Response.success(shareJobResponse)

            // First call returns unknown status, second call returns complete
            val unknownStatusResponseContent = JSONObject().apply {
                put(".tag", "unknown_status")
            }.toString()

            val completedResponseContent = JSONObject().apply {
                put(".tag", "complete")
                put("folder_id", "sf_789")
                put("shared_folder_id", "sf_789")
                put("name", "test folder")
                put("preview_url", "https://preview.url")
            }.toString()

            coEvery { dropboxApiService.checkShareJobStatus(any()) } returnsMany listOf(
                Response.success(createReusableResponseBody(unknownStatusResponseContent)),
                Response.success(createReusableResponseBody(completedResponseContent))
            )

            coEvery { dropboxApiService.addFolderSharedMember(any()) } returns Response.success("{}".toResponseBody())
            // Mock permission listing for final verification - should include the newly created group permission
            val folderSharedMembersWithNewGroup = JSONObject().apply {
                put("users", JSONArray())
                put(
                    "groups",
                    JSONArray().apply {
                        put(
                            JSONObject().apply {
                                put("id", TEST_PERMISSION_GROUP_ID)
                                put("role", "editor")
                                put(
                                    "group",
                                    JSONObject().apply {
                                        put("id", TEST_PERMISSION_GROUP_ID)
                                        put("name", TEST_PERMISSION_GROUP_NAME)
                                    }
                                )
                                put("is_inherited", false)
                            }
                        )
                    }
                )
                put("cursor", "")
                put("has_more", false)
            }
            coEvery { dropboxApiService.listFolderSharedMembers(any()) } returns Response.success(
                folderSharedMembersWithNewGroup.toString().toResponseBody("application/json".toMediaTypeOrNull())
            )
            coEvery { dropboxApiService.continueListFolderSharedMembers(any()) } returns Response.success(
                "{}".toResponseBody()
            )

            val create = OmhCreatePermission.CreateIdentityPermission(
                role = OmhPermissionRole.WRITER,
                recipient = OmhPermissionRecipient.WithObjectId(TEST_PERMISSION_GROUP_ID)
            )

            val result = fileRepositoryImpl.createNodePermission("id:testFolder1", create)

            assertEquals(testOmhGroupPermission, result)

            // Verify unknown status was handled and polling continued
            coVerify(exactly = 2) {
                dropboxApiService.checkShareJobStatus(
                    eq(CheckShareJobStatusRequest("unknown_status_job_123"))
                )
            }
            coVerify {
                dropboxApiService.addFolderSharedMember(
                    withArg { req ->
                        assertEquals("sf_789", req.sharedFolderId)
                        assertEquals(1, req.members.size)
                        assertEquals("dropbox_id", req.members.first().member.tag)
                        assertEquals(TEST_PERMISSION_GROUP_ID, req.members.first().member.userId)
                        assertEquals(OmhPermissionRole.WRITER, req.members.first().accessLevel)
                    }
                )
            }
        }
}
