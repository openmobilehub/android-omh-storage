package com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository

import android.webkit.MimeTypeMap
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.CreatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.UpdatePermissionRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.GrantedToV2
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.Identity
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.ItemReference
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.PermissionResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.PermissionsListResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles.TestListFolderResponse
import com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles.TestListFolderResponse.paginatedFirstPage
import com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles.TestListFolderResponse.paginatedSecondPage
import com.openmobilehub.android.storage.plugin.onedrive.restful.testdoubles.TestListFolderResponse.rootSinglePage
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.File

@Suppress(
    "LargeClass",
    "UnusedPrivateMember",
    "ForbiddenComment",
    "MaxLineLength",
    "MaximumLineLength",
    "LongMethod"
)
@OptIn(ExperimentalCoroutinesApi::class)
class OneDriveRestfulFileRepositoryTest {

    companion object {
        private const val TEST_MIME_TYPE = "application/x-test-mimetype"
    }

    @MockK(relaxed = true)
    private lateinit var mimeTypeMap: MimeTypeMap

    @MockK
    private lateinit var apiService: OneDriveApiService

    private lateinit var repository: OneDriveRestfulFileRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(MimeTypeMap::class)
        every { MimeTypeMap.getSingleton() } returns mimeTypeMap
        every { mimeTypeMap.getMimeTypeFromExtension(any()) } returns TEST_MIME_TYPE
        repository = OneDriveRestfulFileRepository(apiService, OkHttpClient())
    }

    @Test
    fun `given root parentId when listFiles single page then returns mapped entities`() = runTest {
        // Arrange
        coEvery {
            apiService.getRootFileList(null)
        } returns Response.success(rootSinglePage)

        // Act
        val result = repository.getFilesList(OneDriveApiService.ROOT)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result[0] is OmhStorageEntity.OmhFile)
        assertTrue(result[1] is OmhStorageEntity.OmhFolder)
        assertEquals("File 1", result[0].name)
        assertEquals("Folder 1", result[1].name)
    }

    @Test
    fun `given folder id when listFiles paginated then aggregates all pages`() = runTest {
        // Arrange
        val folderId = "FOLDER123"
        coEvery {
            apiService.getFolderFileList(folderId, null)
        } returns Response.success(paginatedFirstPage)
        coEvery {
            apiService.getFolderFileList(folderId, "abc")
        } returns Response.success(paginatedSecondPage)

        // Act
        val result = repository.getFilesList(folderId)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result[0] is OmhStorageEntity.OmhFile)
        assertTrue(result[1] is OmhStorageEntity.OmhFolder)
        assertEquals(TestListFolderResponse.page1File.name, result[0].name)
        assertEquals(TestListFolderResponse.page2Folder.name, result[1].name)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when listing then throws ApiException`() = runTest {
        // Arrange
        coEvery {
            apiService.getFolderFileList("BAD_ID", null)
        } returns Response.error(
            404,
            "not found".toResponseBody("application/json".toMediaType())
        )

        // Act
        repository.getFilesList("BAD_ID")
    }

    @Test
    fun `given valid fileId when deleteFile succeeds then return true`() = runTest {
        // Arrange
        coEvery { apiService.deleteFile("FILE123") } returns Response.success(Unit)

        // Act
        val result = repository.deleteFile("FILE123")

        // Assert
        assertTrue(result)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when deleteFile then throws ApiException`() = runTest {
        // Arrange
        coEvery { apiService.deleteFile("BAD_ID") } returns Response.error(
            400,
            "bad request".toResponseBody("text/plain".toMediaType())
        )

        // Act
        repository.deleteFile("BAD_ID")
    }

    @Test
    fun `given small file when uploadFile then uses simple upload endpoint with full body`() = runTest {
        // Arrange
        val mockedClient = OkHttpClient() // not used in simple upload path
        val repo = OneDriveRestfulFileRepository(apiService, mockedClient)
        val tmp = File.createTempFile("small-upload", ".txt").apply {
            writeText("hello-world") // 11 bytes
            deleteOnExit()
        }
        val capturedPath = slot<String>()
        val capturedBody = slot<okhttp3.RequestBody>()
        coEvery {
            apiService.uploadFile(capture(capturedPath), capture(capturedBody))
        } returns Response.success(
            TestListFolderResponse.testFile1
        )

        // Act
        val result = repo.uploadFile(tmp, null)

        // Assert
        assertTrue(result is OmhStorageEntity.OmhFile)
        assertEquals("/${tmp.name}", capturedPath.captured)
        assertEquals(tmp.length(), capturedBody.captured.contentLength())
    }

    // Permissions: getNodePermission

    @Test
    fun `given permissions for user and group when getNodePermission then maps to OmhPermission list`() = runTest {
        // Arrange
        val itemId = "ITEM123"
        val userPerm = PermissionResponse(
            id = "perm1",
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U1", displayName = "Alice", email = "alice@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        val groupPerm = PermissionResponse(
            id = "perm2",
            roles = listOf("write"),
            grantedToV2 = GrantedToV2(
                user = null,
                group = Identity(id = "G1", displayName = "Team", email = "team@example.com")
            ),
            inheritedFrom = ItemReference(id = "PARENT1"),
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(userPerm, groupPerm))
        )

        // Act
        val result = repository.getNodePermission(itemId)

        // Assert
        assertEquals(2, result.size)
        val p1 = result[0] as OmhPermission.IdentityPermission
        assertEquals("perm1", p1.id)
        assertEquals(OmhPermissionRole.READER, p1.role)
        assertTrue(p1.identity is OmhIdentity.User)
        assertEquals(false, p1.isInherited)

        val p2 = result[1] as OmhPermission.IdentityPermission
        assertEquals("perm2", p2.id)
        assertEquals(OmhPermissionRole.WRITER, p2.role)
        assertTrue(p2.identity is OmhIdentity.Group)
        assertEquals(true, p2.isInherited)
    }

    @Test
    fun `given roles include owner when getNodePermission then most permissive role mapped`() = runTest {
        // Arrange
        val itemId = "ITEM456"
        val ownerPerm = PermissionResponse(
            id = "perm3",
            roles = listOf("read", "owner"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U2", displayName = "Bob", email = "bob@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(ownerPerm))
        )

        // Act
        val result = repository.getNodePermission(itemId)

        // Assert
        assertEquals(1, result.size)
        val p = result.first() as OmhPermission.IdentityPermission
        assertEquals(OmhPermissionRole.OWNER, p.role)
    }

    @Test
    fun `given invalid entries when getNodePermission then filters them out`() = runTest {
        // Arrange
        val itemId = "ITEM789"
        val noRoles = PermissionResponse(
            id = "perm4",
            roles = null,
            grantedToV2 = GrantedToV2(
                user = Identity("U3", "Cara", "cara@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        val noGrantee = PermissionResponse(
            id = "perm5",
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(user = null, group = null),
            inheritedFrom = null,
            link = null
        )
        val valid = PermissionResponse(
            id = "perm6",
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(
                user = Identity(
                    "U4",
                    "Dan",
                    "dan@example.com"
                ),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(noRoles, noGrantee, valid))
        )

        // Act
        val result = repository.getNodePermission(itemId)

        // Assert
        assertEquals(1, result.size)
        val p = result.first() as OmhPermission.IdentityPermission
        assertEquals("perm6", p.id)
        assertEquals(OmhPermissionRole.READER, p.role)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when getNodePermission then throws ApiException`() = runTest {
        // Arrange
        val itemId = "BAD_PERMS"
        coEvery { apiService.getItemPermissions(itemId) } returns Response.error(
            403,
            "forbidden".toResponseBody("application/json".toMediaType())
        )

        // Act
        repository.getNodePermission(itemId)
    }

    // Permissions: createNodePermission

    @Test
    fun `given user recipient when createNodePermission returns list then returns mapped first`() = runTest {
        // Arrange
        val itemId = "ITEM_CREATE_1"
        val create = OmhCreatePermission.CreateIdentityPermission(
            recipient = OmhPermissionRecipient.User(emailAddress = "alice@example.com"),
            role = OmhPermissionRole.READER
        )
        val bodySlot = slot<CreatePermissionRequestBody>()
        val createdPerm = PermissionResponse(
            id = "permC1",
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U1", displayName = "Alice", email = "alice@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery {
            apiService.createPermission(itemId, capture(bodySlot))
        } returns Response.success(
            PermissionsListResponse(listOf(createdPerm))
        )

        // Act
        val result = repository.createNodePermission(
            id = itemId,
            permission = create,
            sendNotificationEmail = true,
            emailMessage = null
        )

        // Assert
        val p = result as OmhPermission.IdentityPermission
        assertEquals("permC1", p.id)
        assertEquals(OmhPermissionRole.READER, p.role)
        assertTrue(p.identity is OmhIdentity.User)
        // Verify request body
        assertEquals(listOf("read"), bodySlot.captured.roles)
        assertEquals(1, bodySlot.captured.recipients.size)
        assertEquals("alice@example.com", bodySlot.captured.recipients.first().email)
        assertTrue(bodySlot.captured.sendInvitation)
        assertEquals(true, bodySlot.captured.requireSignIn)
        assertEquals(null, bodySlot.captured.message)
    }

    @Test
    fun `given empty response when createNodePermission then falls back to refetch and returns created`() = runTest {
        // Arrange
        val itemId = "ITEM_CREATE_2"
        val create = OmhCreatePermission.CreateIdentityPermission(
            recipient = OmhPermissionRecipient.User(emailAddress = "bob@example.com"),
            role = OmhPermissionRole.WRITER
        )
        val bodySlot = slot<CreatePermissionRequestBody>()
        // First call returns empty list
        coEvery { apiService.createPermission(itemId, capture(bodySlot)) } returns Response.success(
            PermissionsListResponse(emptyList())
        )
        // Subsequent permission fetch returns the created one
        val refetched = PermissionResponse(
            id = "permC2",
            roles = listOf("write"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U2", displayName = "Bob", email = "bob@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(refetched))
        )

        // Act
        val result = repository.createNodePermission(
            id = itemId,
            permission = create,
            sendNotificationEmail = false,
            emailMessage = "Welcome"
        )

        // Assert
        val p = result as OmhPermission.IdentityPermission
        assertEquals("permC2", p.id)
        assertEquals(OmhPermissionRole.WRITER, p.role)
        assertTrue(p.identity is OmhIdentity.User)
        // Verify request body
        assertEquals(listOf("write"), bodySlot.captured.roles)
        assertEquals(1, bodySlot.captured.recipients.size)
        assertEquals("bob@example.com", bodySlot.captured.recipients.first().email)
        assertEquals(false, bodySlot.captured.sendInvitation)
        assertEquals("Welcome", bodySlot.captured.message)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when createNodePermission then throws ApiException`() = runTest {
        // Arrange
        val itemId = "ITEM_CREATE_ERR"
        val create = OmhCreatePermission.CreateIdentityPermission(
            recipient = OmhPermissionRecipient.User(emailAddress = "err@example.com"),
            role = OmhPermissionRole.READER
        )
        coEvery { apiService.createPermission(itemId, any()) } returns Response.error(
            400,
            "bad request".toResponseBody("application/json".toMediaType())
        )

        // Act
        repository.createNodePermission(
            id = itemId,
            permission = create,
            sendNotificationEmail = true,
            emailMessage = null
        )
    }

    @Test
    @Suppress("MaxLineLength", "MaximumLineLength")
    fun `given group recipient when createNodePermission returns list then returns mapped group permission`() =
        runTest {
            // Arrange
            val itemId = "ITEM_CREATE_G1"
            val create = OmhCreatePermission.CreateIdentityPermission(
                recipient = OmhPermissionRecipient.Group(emailAddress = "team@example.com"),
                role = OmhPermissionRole.READER
            )
            val bodySlot = slot<CreatePermissionRequestBody>()
            val createdPerm = PermissionResponse(
                id = "permG1",
                roles = listOf("read"),
                grantedToV2 = GrantedToV2(
                    user = null,
                    group = Identity(id = "G1", displayName = "Team", email = "team@example.com")
                ),
                inheritedFrom = null,
                link = null
            )
            coEvery { apiService.createPermission(itemId, capture(bodySlot)) } returns Response.success(
                PermissionsListResponse(listOf(createdPerm))
            )

            // Act
            val result = repository.createNodePermission(
                id = itemId,
                permission = create,
                sendNotificationEmail = true,
                emailMessage = null
            )

            // Assert
            val p = result as OmhPermission.IdentityPermission
            assertEquals("permG1", p.id)
            assertEquals(OmhPermissionRole.READER, p.role)
            assertTrue(p.identity is OmhIdentity.Group)
            // Verify request body
            assertEquals(listOf("read"), bodySlot.captured.roles)
            assertEquals(1, bodySlot.captured.recipients.size)
            assertEquals("team@example.com", bodySlot.captured.recipients.first().email)
        }

    // Permissions: updateNodePermission

    @Test
    fun `given update returns mappable permission then updateNodePermission returns mapped`() = runTest {
        // Arrange
        val itemId = "ITEM_UPD_1"
        val permissionId = "permU1"
        val bodySlot = slot<UpdatePermissionRequestBody>()
        val responsePerm = PermissionResponse(
            id = permissionId,
            roles = listOf("write"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U10", displayName = "User10", email = "u10@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.updatePermission(itemId, permissionId, capture(bodySlot)) } returns Response.success(
            responsePerm
        )

        // Act
        val result = repository.updateNodePermission(itemId, permissionId, OmhPermissionRole.WRITER)

        // Assert
        val p = result as OmhPermission.IdentityPermission
        assertEquals(permissionId, p.id)
        assertEquals(OmhPermissionRole.WRITER, p.role)
        assertTrue(p.identity is OmhIdentity.User)
        assertEquals(listOf("write"), bodySlot.captured.roles)
    }

    @Test
    fun `given update returns unmappable then updateNodePermission falls back and returns refetched`() = runTest {
        // Arrange
        val itemId = "ITEM_UPD_2"
        val permissionId = "permU2"
        val unmappable = PermissionResponse(
            id = null, // triggers mapper to return null
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U20", displayName = "User20", email = "u20@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery {
            apiService.updatePermission(itemId, permissionId, any())
        } returns Response.success(
            unmappable
        )
        val refetched = PermissionResponse(
            id = permissionId,
            roles = listOf("read"),
            grantedToV2 = GrantedToV2(
                user = Identity(id = "U20", displayName = "User20", email = "u20@example.com"),
                group = null
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(refetched))
        )

        // Act
        val result = repository.updateNodePermission(itemId, permissionId, OmhPermissionRole.READER)

        // Assert
        val p = result as OmhPermission.IdentityPermission
        assertEquals(permissionId, p.id)
        assertEquals(OmhPermissionRole.READER, p.role)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when updateNodePermission then throws ApiException`() = runTest {
        // Arrange
        val itemId = "ITEM_UPD_ERR"
        val permissionId = "permUErr"
        coEvery {
            apiService.updatePermission(itemId, permissionId, any())
        } returns Response.error(
            400,
            "bad request".toResponseBody("application/json".toMediaType())
        )

        // Act
        repository.updateNodePermission(itemId, permissionId, OmhPermissionRole.READER)
    }

    // Permissions: deleteNodePermission

    @Test
    fun `given valid ids when deleteNodePermission succeeds then returns true`() = runTest {
        // Arrange
        val itemId = "ITEM_DEL_1"
        val permissionId = "permD1"
        coEvery {
            apiService.deletePermission(itemId, permissionId)
        } returns Response.success(Unit)

        // Act
        val result = repository.deleteNodePermission(itemId, permissionId)

        // Assert
        assertTrue(result)
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `given api error when deleteNodePermission then throws ApiException`() = runTest {
        // Arrange
        val itemId = "ITEM_DEL_ERR"
        val permissionId = "permDErr"
        coEvery { apiService.deletePermission(itemId, permissionId) } returns Response.error(
            403,
            "forbidden".toResponseBody("application/json".toMediaType())
        )

        // Act
        repository.deleteNodePermission(itemId, permissionId)
    }

    @Test
    fun `given big file when uploadFile then splits into chunks and PUTs with correct Content-Range`() = runTest {
        // Arrange: force chunking with 1MB chunk size and create a 3MB file
        val httpClient = mockk<OkHttpClient>()
        val repo = OneDriveRestfulFileRepository(apiService, httpClient, smallFileLimitInMB = 1)
        val file = File.createTempFile("big-upload", ".bin").apply {
            // Write exactly 3 MiB
            val content = ByteArray(3 * 1024 * 1024) { 0x41 }
            writeBytes(content)
            deleteOnExit()
        }
        val uploadUrl = "http://localhost:3000/uploadSession"
        coEvery { apiService.createUploadSession("/${file.name}", any()) } returns Response.success(
            com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.UploadSessionResponse(
                uploadUrl = uploadUrl,
                expirationDateTime = "2099-12-31T23:59:59.999Z"
            )
        )

        val capturedRequests = mutableListOf<Request>()
        var callIndex = 0
        every { httpClient.newCall(any()) } answers {
            val req = firstArg<Request>()
            capturedRequests.add(req)
            val code = if (callIndex < 2) 202 else 200
            callIndex++
            val respBuilder = okhttp3.Response.Builder()
                .code(code)
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .request(req)
            if (code == 200) {
                val finalJson = (
                    "{" +
                        "\"id\":\"FIN123\"," +
                        "\"name\":\"${file.name}\"," +
                        "\"createdDateTime\":\"2024-01-01T00:00:00.000Z\"," +
                        "\"lastModifiedDateTime\":\"2024-01-01T00:00:00.000Z\"," +
                        "\"size\":${file.length()}," +
                        "\"file\":{\"mimeType\":\"application/octet-stream\"}," +
                        "\"folder\":null," +
                        "\"@microsoft.graph.downloadUrl\":null," +
                        "\"parentReference\":{\"id\":\"PARENT\",\"driveId\":\"DRIVE\",\"path\":\"/drive/root:\"}," +
                        "\"webUrl\":\"http://localhost/${file.name}\"" +
                        "}"
                    ).toResponseBody("application/json".toMediaTypeOrNull())
                respBuilder.body(finalJson)
            }
            val call = mockk<okhttp3.Call>()
            every { call.execute() } returns respBuilder.build()
            call
        }

        // Act
        val result = repo.uploadFile(file, null)

        // Assert: verify three chunk PUTs with correct headers
        assertTrue(result is OmhStorageEntity.OmhFile)
        assertEquals(file.name, result?.name)
        assertEquals(file.length().toInt(), (result as OmhStorageEntity.OmhFile).size)
        assertEquals(3, capturedRequests.size)

        // Chunk size 1 MiB
        val oneMiB = 1024 * 1024
        val total = 3 * oneMiB
        fun assertChunk(req: Request, start: Int, endInclusive: Int) {
            assertEquals(uploadUrl, req.url.toString())
            assertEquals("PUT", req.method)
            assertEquals(oneMiB.toString(), req.header("Content-Length"))
            assertEquals("bytes $start-$endInclusive/$total", req.header("Content-Range"))
            assertEquals(oneMiB.toLong(), req.body?.contentLength())
        }
        assertChunk(capturedRequests[0], 0, oneMiB - 1)
        assertChunk(capturedRequests[1], oneMiB, (2 * oneMiB) - 1)
        assertChunk(capturedRequests[2], 2 * oneMiB, (3 * oneMiB) - 1)
    }

    // Download file

    @Test
    fun `given valid fileId when downloadFile then returns bytes and caches url`() = runTest {
        // Arrange
        val http = mockk<OkHttpClient>()
        val repo = OneDriveRestfulFileRepository(apiService, http)
        val fileId = "FILE123"
        val downloadUrl = "http://localhost:3000/download/$fileId"
        val item = DriveItem(
            id = fileId,
            name = "file.txt",
            createdAt = "2024-01-01T00:00:00.000Z",
            updatedAt = "2024-01-01T00:00:00.000Z",
            size = 11,
            fileInfo = DriveItem.FileInfo(
                mimeType = "text/plain"
            ),
            folderInfo = null,
            fileDownloadUrl = downloadUrl,
            parentReference = DriveItem.ParentReference(
                id = "PARENT",
                driveId = "DRIVE",
                path = "/drive/root:"
            ),
            webUrl = "http://localhost/file.txt"
        )
        coEvery { apiService.getItemById(fileId) } returns Response.success(item)

        val bytes = "hello world".toByteArray()
        every { http.newCall(any()) } answers {
            val req = firstArg<Request>()
            val call = mockk<okhttp3.Call>()
            val resp = okhttp3.Response.Builder()
                .code(200)
                .protocol(Protocol.HTTP_1_1)
                .message("")
                .request(req)
                .body(bytes.toResponseBody("application/octet-stream".toMediaTypeOrNull()))
                .build()
            every { call.execute() } returns resp
            call
        }

        // Act: call twice to exercise cache
        val out1 = repo.downloadFile(fileId)
        val out2 = repo.downloadFile(fileId)

        // Assert
        assertArrayEquals(bytes, out1.toByteArray())
        assertArrayEquals(bytes, out2.toByteArray())
        coVerify(exactly = 1) { apiService.getItemById(fileId) }
    }

    @Test(expected = OmhStorageException.DownloadException::class)
    fun `given missing fileId when downloadFile then throws DownloadException`() = runTest {
        // Arrange
        val http = OkHttpClient()
        val repo = OneDriveRestfulFileRepository(apiService, http)
        val fileId = "MISSING"
        coEvery { apiService.getItemById(fileId) } returns Response.error(
            404,
            "not found".toResponseBody("application/json".toMediaType())
        )

        // Act
        repo.downloadFile(fileId)
    }

    @Test
    fun `given siteUser inside grantedToIdentitiesV2 when getNodePermission then maps to user identity`() = runTest {
        val itemId = "ITEM_SITEUSER"
        val perm = PermissionResponse(
            id = "permSite",
            roles = listOf("write"),
            grantedToV2 = null,
            grantedToIdentitiesV2 = listOf(
                GrantedToV2(
                    user = null,
                    group = null,
                    siteUser = Identity(id = "SU1", displayName = "Site User 1", email = "su1@example.com"),
                    siteGroup = null
                )
            ),
            inheritedFrom = null,
            link = null
        )
        coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
            PermissionsListResponse(listOf(perm))
        )

        val result = repository.getNodePermission(itemId)
        assertEquals(1, result.size)
        val p = result.first() as OmhPermission.IdentityPermission
        assertEquals("permSite", p.id)
        assertEquals(OmhPermissionRole.WRITER, p.role)
        assertTrue(p.identity is OmhIdentity.User)
        assertEquals("Site User 1", (p.identity as OmhIdentity.User).displayName)
    }

    @Test
    fun `given multiple grantedToIdentitiesV2 entries when getNodePermission then returns multiple permissions`() =
        runTest {
            val itemId = "ITEM_MULTI_IDENTITIES"
            val perm = PermissionResponse(
                id = "permMulti",
                roles = listOf("read"),
                grantedToV2 = null,
                grantedToIdentitiesV2 = listOf(
                    GrantedToV2(
                        user = Identity(id = "U100", displayName = "User100", email = "u100@example.com"),
                        group = null,
                        siteUser = null,
                        siteGroup = null
                    ),
                    GrantedToV2(
                        user = null,
                        group = Identity(id = "G200", displayName = "Group200", email = "g200@example.com"),
                        siteUser = null,
                        siteGroup = null
                    ),
                    GrantedToV2(
                        user = null,
                        group = null,
                        siteUser = Identity(id = "SU300", displayName = "SiteUser300", email = "su300@example.com"),
                        siteGroup = null
                    )
                ),
                inheritedFrom = null,
                link = null
            )
            coEvery { apiService.getItemPermissions(itemId) } returns Response.success(
                PermissionsListResponse(listOf(perm))
            )

            val result = repository.getNodePermission(itemId)
            // Expect 3 permissions (user, group, siteUser treated as user)
            assertEquals(3, result.size)
            val identities = result.map { (it as OmhPermission.IdentityPermission).identity }
            assertTrue(identities.any { it is OmhIdentity.User && it.id == "U100" })
            assertTrue(identities.any { it is OmhIdentity.Group && it.id == "G200" })
            assertTrue(identities.any { it is OmhIdentity.User && it.id == "SU300" })
        }
}
