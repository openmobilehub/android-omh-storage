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

package com.openmobilehub.android.storage.plugin.dropbox.data.repository

import com.dropbox.core.DbxApiException
import com.dropbox.core.v2.async.LaunchResultBase
import com.dropbox.core.v2.files.CreateFolderResult
import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.ListRevisionsResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.RelocationResult
import com.dropbox.core.v2.files.SearchMatchV2
import com.dropbox.core.v2.files.SearchV2Result
import com.dropbox.core.v2.sharing.AccessLevel
import com.dropbox.core.v2.sharing.FileMemberActionIndividualResult
import com.dropbox.core.v2.sharing.FileMemberActionResult
import com.dropbox.core.v2.sharing.FileMemberRemoveActionResult
import com.dropbox.core.v2.sharing.GroupInfo
import com.dropbox.core.v2.sharing.GroupMembershipInfo
import com.dropbox.core.v2.sharing.InviteeInfo
import com.dropbox.core.v2.sharing.InviteeMembershipInfo
import com.dropbox.core.v2.sharing.MemberAccessLevelResult
import com.dropbox.core.v2.sharing.ShareFolderJobStatus
import com.dropbox.core.v2.sharing.ShareFolderLaunch
import com.dropbox.core.v2.sharing.SharedFileMembers
import com.dropbox.core.v2.sharing.SharedFolderMembers
import com.dropbox.core.v2.sharing.SharedFolderMetadata
import com.dropbox.core.v2.sharing.UserFileMembershipInfo
import com.dropbox.core.v2.sharing.UserInfo
import com.dropbox.core.v2.users.SpaceUsage
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.utils.toInputStream
import com.openmobilehub.android.storage.plugin.dropbox.DropboxConstants
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.MetadataToOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.dropbox.data.mapper.toOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.data.service.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_EMAIL_MESSAGE
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_EXTENSION
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_MODIFIED_TIME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PARENT_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_PATH
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FILE_WEB_URL
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_FOLDER_NAME
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_PERMISSION_EMAIL_ADDRESS
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_PERMISSION_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_SHARED_FOLDER_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_FILE_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.TEST_VERSION_ID
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.addUserMember
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.createUserPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.dropboxIdMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.emailMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setUpMock
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setUpMockForPersonalAccount
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setupMockForOtherAccount
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.setupMockForTeamAccount
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testFileJpg
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testInvitedOmhPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhFolder
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhGroupPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhUserPermission
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testOmhVersion
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testQueryFolder1
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testQueryFolder2
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testQueryFolder3
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testQueryFolderRsx
import com.openmobilehub.android.storage.plugin.dropbox.testdoubles.testQueryRootFolder
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import kotlin.test.assertNull

@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
class DropboxFileRepositoryTest {

    @MockK
    private lateinit var omhStorageEntity: OmhStorageEntity

    @MockK
    private lateinit var dropboxFiles: ListFolderResult

    @MockK
    private lateinit var dropboxRevisions: ListRevisionsResult

    @MockK
    private lateinit var deleteResult: DeleteResult

    @MockK
    lateinit var searchResult: SearchV2Result

    @MockK
    private lateinit var createFolderResult: CreateFolderResult

    @MockK
    private lateinit var relocationResult: RelocationResult

    @MockK(relaxed = true)
    private lateinit var apiService: DropboxApiService

    @MockK
    private lateinit var metadataToOmhStorageEntity: MetadataToOmhStorageEntity

    @MockK(relaxed = true)
    private lateinit var file: File

    @MockK
    private lateinit var fileMetadata: FileMetadata

    @MockK(relaxed = true)
    private lateinit var folderMetadata: FolderMetadata

    @MockK(relaxed = true)
    private lateinit var shareFolderLaunch: ShareFolderLaunch

    @MockK
    private lateinit var shareFolderJobStatus: ShareFolderJobStatus

    @MockK
    private lateinit var sharedFolderMetadata: SharedFolderMetadata

    @MockK
    private lateinit var fileMemberActionResult: FileMemberActionResult

    @MockK
    private lateinit var fileMemberActionIndividualResult: FileMemberActionIndividualResult

    @MockK
    private lateinit var sharedFolderMembers: SharedFolderMembers

    @MockK
    private lateinit var sharedFileMembers: SharedFileMembers

    @MockK
    private lateinit var userMembershipInfo: UserFileMembershipInfo

    @MockK
    private lateinit var userInfo: UserInfo

    @MockK
    private lateinit var groupMembershipInfo: GroupMembershipInfo

    @MockK
    private lateinit var inviteeMembershipInfo: InviteeMembershipInfo

    @MockK
    private lateinit var inviteeInfo: InviteeInfo

    @MockK
    private lateinit var groupInfo: GroupInfo

    @MockK
    private lateinit var launchResultBase: LaunchResultBase

    @MockK
    private lateinit var fileMemberRemoveActionResult: FileMemberRemoveActionResult

    @MockK
    private lateinit var memberAccessLevelResult: MemberAccessLevelResult

    @MockK
    private lateinit var metadata: Metadata

    @MockK
    private lateinit var searchMatch: SearchMatchV2

    @MockK(relaxed = true)
    private lateinit var spaceUsage: SpaceUsage

    @MockK(relaxed = true)
    private lateinit var dbxApiException: DbxApiException

    private lateinit var repository: DropboxFileRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic("com.openmobilehub.android.storage.core.utils.FileExtensionsKt")
        every { file.toInputStream() } returns mockk<FileInputStream>()

        mockkStatic("com.openmobilehub.android.storage.plugin.dropbox.data.mapper.VersionMapperKt")
        mockkStatic("com.openmobilehub.android.storage.plugin.dropbox.data.mapper.FolderMapperKt")

        userInfo.setUpMock()
        userMembershipInfo.setUpMock(userInfo)
        groupInfo.setUpMock()
        groupMembershipInfo.setUpMock(groupInfo)

        inviteeInfo.setUpMock()
        inviteeMembershipInfo.setUpMock(inviteeInfo)

        repository = DropboxFileRepository(apiService, metadataToOmhStorageEntity)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `given an apiService returns a non-empty list, when getting the files list, then return a non-empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns listOf(mockk())
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns an empty list, when getting the files list, then return an empty list`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns emptyList()

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(emptyList<OmhStorageEntity>(), result)
    }

    @Test
    fun `given some entries return null from metadataToOmhFile, when getting the files list, then return only non-null entries`() {
        // Arrange
        every { apiService.getFilesList(TEST_FILE_PARENT_ID) } returns dropboxFiles

        every { dropboxFiles.entries } returns listOf(mockk(), mockk())
        every { metadataToOmhStorageEntity(any()) } returnsMany listOf(omhStorageEntity, null)

        // Act
        val result = repository.getFilesList(TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an api service returns FileMetadata, when uploading the file, then returns OmhFile`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns mockk<FileMetadata>()
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.uploadFile(file, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an api service return FileMetadata, when downloading the file, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFile(any(), any()) } returns fileMetadata

        // Act
        val result = repository.downloadFile(TEST_FILE_ID)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `given an api service returns a non-empty list, when getting the file versions, then return a non-empty list`() {
        // Arrange
        every { apiService.getFileRevisions(any()) } returns dropboxRevisions
        every { dropboxRevisions.entries } returns listOf(fileMetadata, fileMetadata)

        every { fileMetadata.toOmhVersion() } returns testOmhVersion

        // Act
        val result = repository.getFileVersions(TEST_VERSION_FILE_ID)

        // Assert
        assertEquals(listOf(testOmhVersion, testOmhVersion), result)
    }

    @Test
    fun `given an api service return FileMetadata, when downloading the file version, then returns ByteArrayOutputStream`() {
        // Arrange
        every { apiService.downloadFileRevision(any(), any()) } returns fileMetadata

        // Act
        val result = repository.downloadFileVersion(TEST_VERSION_ID)

        // Assert
        assertNotNull(result)
    }

    @Test
    fun `given an api service return DeleteResult, when deleting the file, then exceptions is not thrown`() {
        // Arrange
        every { apiService.deleteFile(any()) } returns deleteResult

        // Act & Assert
        repository.deleteFile(TEST_FILE_ID)
    }

    @Test
    fun `given an apiService returns a non-empty list, when searching the files, then return a non-empty list`() {
        // Arrange
        every { apiService.search(any()) } returns searchResult
        every { searchResult.matches } returns listOf(searchMatch)
        every { searchMatch.metadata.metadataValue } returns fileMetadata

        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.search("test")

        // Assert
        assertEquals(listOf(omhStorageEntity), result)
    }

    @Test
    fun `given an apiService returns Metadata that can be mapped to OmhStorageEntity, when getting the file, then return OmhStorageMetadata`() {
        // Arrange
        every { apiService.getFile(any()) } returns metadata
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.getFileMetadata(TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result.entity)
        assertEquals(metadata, result.originalMetadata)
    }

    @Test
    fun `given an apiService returns Metadata that cannot be mapped to OmhStorageEntity, when getting the file, then error is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns metadata
        every { metadataToOmhStorageEntity(any()) } returns null

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.getFileMetadata(TEST_FILE_ID)
        }
    }

    @Test
    fun `given an apiService returns FolderMetadata, when creating a folder in nested folder, then return OmhStorageEntity`() {
        // Arrange
        val nestedFolderPath = "/folder"
        val expectedPath = "$nestedFolderPath/$TEST_FOLDER_NAME"

        val slot = slot<String>()
        every { apiService.createFolder(capture(slot)) } returns createFolderResult
        every { createFolderResult.metadata } returns folderMetadata

        every { apiService.getFile(any()) } returns metadata
        every { metadata.pathLower } returns nestedFolderPath

        every { folderMetadata.toOmhStorageEntity() } returns testOmhFolder

        // Act
        val result = repository.createFolder(TEST_FOLDER_NAME, TEST_FILE_PARENT_ID)

        // Assert
        assertEquals(testOmhFolder, result)
        assertEquals(slot.captured, expectedPath)
    }

    @Test
    fun `given an apiService returns FolderMetadata, when creating a folder in root, then return OmhStorageEntity`() {
        // Arrange
        val expectedPath = "/$TEST_FOLDER_NAME"

        val slot = slot<String>()
        every { apiService.createFolder(capture(slot)) } returns createFolderResult

        every { createFolderResult.metadata } returns folderMetadata
        every { folderMetadata.toOmhStorageEntity() } returns testOmhFolder

        // Act
        val result = repository.createFolder(TEST_FOLDER_NAME, DropboxConstants.ROOT_FOLDER)

        // Assert
        assertEquals(testOmhFolder, result)
        assertEquals(slot.captured, expectedPath)
    }

    @Test
    fun `given an apiService does not return pathLower, when getting the new folder path, then ApiException is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns metadata
        every { metadata.pathLower } returns null

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.getNewFolderPath(TEST_FILE_PARENT_ID, TEST_FOLDER_NAME)
        }
    }

    @Test
    fun `given an apiService throw an exception, when creating a folder, then ApiException is thrown`() {
        // Arrange
        every { apiService.createFolder(any()) } throws dbxApiException

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.createFolder(TEST_FILE_PARENT_ID, DropboxConstants.ROOT_FOLDER)
        }
    }

    @Test
    fun `given an apiService returns a file metadata, when creating a file, then return an OmhStorageEntity`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } returns fileMetadata
        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.createFileWithExtension(
            TEST_FILE_NAME,
            TEST_FILE_EXTENSION,
            TEST_FILE_PARENT_ID
        )

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an apiService throws an exception, when creating a file, then an ApiException is thrown`() {
        // Arrange
        every { apiService.uploadFile(any(), any()) } throws dbxApiException

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.createFileWithExtension(
                TEST_FILE_NAME,
                TEST_FILE_EXTENSION,
                TEST_FILE_PARENT_ID
            )
        }
    }

    @Test
    fun `given a new name is the same as existing, when renaming the file, then move file is not called`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { fileMetadata.pathLower } returns TEST_FILE_PATH
        every { fileMetadata.name } returns TEST_FILE_NAME

        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.renameFile(TEST_FILE_ID, TEST_FILE_NAME)

        // Assert
        verify(exactly = 0) { apiService.moveFile(any(), any()) }
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given a new name is not the same as existing, when renaming the file, then move file is called`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { fileMetadata.pathLower } returns TEST_FILE_PATH
        every { fileMetadata.name } returns TEST_FILE_NAME

        every { apiService.moveFile(any(), any()) } returns relocationResult
        every { relocationResult.metadata } returns fileMetadata

        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        val updatedName = "updatedName.txt"

        // Act
        val result = repository.renameFile(TEST_FILE_ID, updatedName)

        // Assert
        verify(exactly = 1) { apiService.moveFile(any(), any()) }
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an apiService throws an exception, when renaming a file, then an ApiException is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } throws dbxApiException

        val updatedName = "updatedName.txt"

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.renameFile(
                TEST_FILE_ID,
                updatedName,
            )
        }
    }

    @Test
    fun `given an api service returns file metadata, when updating the file, then return OmhStorageEntity`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { fileMetadata.pathLower } returns TEST_FILE_PATH
        every { fileMetadata.name } returns TEST_FILE_NAME

        every { apiService.uploadFile(any(), any(), any(), any()) } returns fileMetadata

        every { apiService.moveFile(any(), any()) } returns relocationResult
        every { relocationResult.metadata } returns fileMetadata

        every { metadataToOmhStorageEntity(any()) } returns omhStorageEntity

        // Act
        val result = repository.updateFile(file, TEST_FILE_ID)

        // Assert
        assertEquals(omhStorageEntity, result)
    }

    @Test
    fun `given an apiService throws an exception, when updating a file, then an ApiException is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { fileMetadata.pathLower } returns TEST_FILE_PATH
        every { fileMetadata.name } returns TEST_FILE_NAME

        every { apiService.uploadFile(any(), any(), any(), any()) } throws dbxApiException

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.updateFile(
                file,
                TEST_FILE_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to not a shared folder, when creating permission, then make it a shared folder`() =
        runTest {
            // Arrange
            every { apiService.getFile(any()) } returns folderMetadata
            every { apiService.shareFolder(any()) } returns shareFolderLaunch
            every { shareFolderLaunch.isComplete } returns true
            every { shareFolderLaunch.completeValue } returns sharedFolderMetadata
            every { sharedFolderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

            every { folderMetadata.sharedFolderId } returns null

            // Act
            repository.createPermission(
                TEST_FILE_ID,
                createUserPermission,
                true,
                TEST_EMAIL_MESSAGE
            )

            // Assert
            verify { apiService.shareFolder(TEST_FILE_ID) }
            verify {
                apiService.createFolderPermission(
                    TEST_SHARED_FOLDER_ID,
                    addUserMember,
                    true,
                    TEST_EMAIL_MESSAGE
                )
            }
        }

    @Test
    fun `given sharing folder is an async job, when creating permission, then create permission after it finishes`() =
        runTest {
            // Arrange
            every { apiService.getFile(any()) } returns folderMetadata
            every { apiService.shareFolder(any()) } returns shareFolderLaunch
            every { apiService.checkShareFolderJobStatus(any()) } returns shareFolderJobStatus
            every { shareFolderJobStatus.isComplete } returns true
            every { shareFolderJobStatus.completeValue } returns sharedFolderMetadata
            every { shareFolderLaunch.isComplete } returns false
            every { sharedFolderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID
            every { folderMetadata.sharedFolderId } returns null

            // Act
            repository.createPermission(
                TEST_FILE_ID,
                createUserPermission,
                true,
                TEST_EMAIL_MESSAGE
            )

            // Assert
            verify { apiService.shareFolder(TEST_FILE_ID) }
            verify {
                apiService.createFolderPermission(
                    TEST_SHARED_FOLDER_ID,
                    addUserMember,
                    true,
                    TEST_EMAIL_MESSAGE
                )
            }
        }

    @Test
    fun `given a fileId belongs to a shared folder, when creating permission, then create permission for folder`() =
        runTest {
            // Arrange
            every { apiService.getFile(any()) } returns folderMetadata
            every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

            // Act
            repository.createPermission(
                TEST_FILE_ID,
                createUserPermission,
                true,
                TEST_EMAIL_MESSAGE
            )

            // Assert
            verify {
                apiService.createFolderPermission(
                    TEST_SHARED_FOLDER_ID,
                    addUserMember,
                    true,
                    TEST_EMAIL_MESSAGE
                )
            }
        }

    @Test
    fun `given a fileId belongs to a file, when creating permission, then create permission for file`() =
        runTest {
            // Arrange
            every { apiService.getFile(any()) } returns fileMetadata
            every {
                apiService.createFilePermission(
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns listOf(
                fileMemberActionResult
            )
            every { fileMemberActionResult.result } returns fileMemberActionIndividualResult
            every { fileMemberActionIndividualResult.isSuccess } returns true

            // Act
            repository.createPermission(
                TEST_FILE_ID,
                createUserPermission,
                true,
                TEST_EMAIL_MESSAGE
            )

            // Assert
            verify {
                apiService.createFilePermission(
                    TEST_FILE_ID,
                    emailMemberSelector,
                    AccessLevel.VIEWER,
                    true,
                    TEST_EMAIL_MESSAGE
                )
            }
        }

    @Test
    fun `given a fileId belongs to not a shared folder, when getting web URL, then return null`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { folderMetadata.sharedFolderId } returns null

        // Act
        val result = repository.getWebUrl(
            TEST_FILE_ID,
        )

        // Assert
        assertNull(result)
    }

    @Test
    fun `given a fileId belongs to a shared folder, when getting web URL, then return folder web URL`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { apiService.getFolderWebUrl(any()) } returns TEST_FILE_WEB_URL
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        val result = repository.getWebUrl(
            TEST_FILE_ID,
        )

        // Assert
        assertEquals(TEST_FILE_WEB_URL, result)
        verify {
            apiService.getFolderWebUrl(
                TEST_SHARED_FOLDER_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to a file, when getting web URL, then return file web URL`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { apiService.getFileWebUrl(any()) } returns TEST_FILE_WEB_URL

        // Act
        val result = repository.getWebUrl(
            TEST_FILE_ID,
        )

        // Assert
        assertEquals(TEST_FILE_WEB_URL, result)
        verify {
            apiService.getFileWebUrl(
                TEST_FILE_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to not a shared folder, when getting permissions, then return empty list`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { folderMetadata.sharedFolderId } returns null

        // Act
        val result = repository.getPermissions(
            TEST_FILE_ID,
        )

        // Assert
        assertEquals(emptyList<OmhPermission>(), result)
    }

    @Test
    fun `given a fileId belongs to a shared folder, when getting permissions, then return folder permissions`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { apiService.getFolderPermissions(any()) } returns sharedFolderMembers
        every { sharedFolderMembers.users } returns listOf(userMembershipInfo)
        every { sharedFolderMembers.groups } returns listOf(groupMembershipInfo)
        every { sharedFolderMembers.invitees } returns listOf(inviteeMembershipInfo)
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        val result = repository.getPermissions(
            TEST_FILE_ID,
        )

        // Assert
        assertEquals(listOf(testInvitedOmhPermission, testOmhUserPermission, testOmhGroupPermission), result)
        verify {
            apiService.getFolderPermissions(
                TEST_SHARED_FOLDER_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to a file, when getting permissions, then return file permissions`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { apiService.getFileWebUrl(any()) } returns TEST_FILE_WEB_URL
        every { apiService.getFilePermissions(any()) } returns sharedFileMembers
        every { sharedFileMembers.users } returns listOf(userMembershipInfo)
        every { sharedFileMembers.groups } returns listOf(groupMembershipInfo)
        every { sharedFileMembers.invitees } returns listOf(inviteeMembershipInfo)

        // Act
        val result = repository.getPermissions(
            TEST_FILE_ID,
        )

        // Assert
        assertEquals(listOf(testInvitedOmhPermission, testOmhUserPermission, testOmhGroupPermission), result)
        verify {
            apiService.getFilePermissions(
                TEST_FILE_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to not a shared folder, when deleting permission, then exception is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { folderMetadata.sharedFolderId } returns null

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.deletePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
            )
        }
    }

    @Test
    fun `given a fileId belongs to a shared folder, when deleting permission, then folder permissions is deleted`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { apiService.deleteFolderPermission(any(), any()) } returns launchResultBase
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        repository.deletePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID
        )

        // Assert
        verify {
            apiService.deleteFolderPermission(
                TEST_SHARED_FOLDER_ID,
                dropboxIdMemberSelector
            )
        }
    }

    @Test
    fun `given a permissionId is an email, when deleting permission, then folder permissions is deleted`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { apiService.deleteFolderPermission(any(), any()) } returns launchResultBase
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        repository.deletePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_EMAIL_ADDRESS
        )

        // Assert
        verify {
            apiService.deleteFolderPermission(
                TEST_SHARED_FOLDER_ID,
                emailMemberSelector
            )
        }
    }

    @Test
    fun `given a fileId belongs to a file, when deleting permission, then file permissions is deleted`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { apiService.deleteFilePermission(any(), any()) } returns fileMemberRemoveActionResult

        // Act
        repository.deletePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID
        )

        // Assert
        verify {
            apiService.deleteFilePermission(
                TEST_FILE_ID,
                dropboxIdMemberSelector
            )
        }
    }

    @Test
    fun `given a permissionId is an email, when deleting permission, then file permissions is deleted`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every { apiService.deleteFilePermission(any(), any()) } returns fileMemberRemoveActionResult

        // Act
        repository.deletePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_EMAIL_ADDRESS
        )

        // Assert
        verify {
            apiService.deleteFilePermission(
                TEST_FILE_ID,
                emailMemberSelector
            )
        }
    }

    @Test
    fun `given a fileId belongs to not a shared folder, when updating permission, then exception is thrown`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every { folderMetadata.sharedFolderId } returns null

        // Act & Assert
        assertThrows(OmhStorageException.ApiException::class.java) {
            repository.updatePermission(
                TEST_FILE_ID,
                TEST_PERMISSION_ID,
                OmhPermissionRole.COMMENTER
            )
        }
    }

    @Test
    fun `given a fileId belongs to a shared folder, when updating permission, then folder permissions is updated`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every {
            apiService.updateFolderPermissions(
                any(),
                any(),
                any()
            )
        } returns memberAccessLevelResult
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID,
            OmhPermissionRole.COMMENTER
        )

        // Assert
        verify {
            apiService.updateFolderPermissions(
                TEST_SHARED_FOLDER_ID,
                dropboxIdMemberSelector,
                AccessLevel.VIEWER
            )
        }
    }

    @Test
    fun `given a permissionId is an email, when updating permission, then folder permissions is updated`() {
        // Arrange
        every { apiService.getFile(any()) } returns folderMetadata
        every {
            apiService.updateFolderPermissions(
                any(),
                any(),
                any()
            )
        } returns memberAccessLevelResult
        every { folderMetadata.sharedFolderId } returns TEST_SHARED_FOLDER_ID

        // Act
        repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID,
            OmhPermissionRole.COMMENTER
        )

        // Assert
        verify {
            apiService.updateFolderPermissions(
                TEST_SHARED_FOLDER_ID,
                dropboxIdMemberSelector,
                AccessLevel.VIEWER
            )
        }
    }

    @Test
    fun `given a fileId belongs to a file, when updating permission, then file permissions is updated`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every {
            apiService.updateFilePermissions(
                any(),
                any(),
                any()
            )
        } returns memberAccessLevelResult

        // Act
        repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_ID,
            OmhPermissionRole.WRITER
        )

        // Assert
        verify {
            apiService.updateFilePermissions(
                TEST_FILE_ID,
                dropboxIdMemberSelector,
                AccessLevel.EDITOR
            )
        }
    }

    @Test
    fun `given a permissionId is an email, when updating permission, then file permissions is updated`() {
        // Arrange
        every { apiService.getFile(any()) } returns fileMetadata
        every {
            apiService.updateFilePermissions(
                any(),
                any(),
                any()
            )
        } returns memberAccessLevelResult

        // Act
        repository.updatePermission(
            TEST_FILE_ID,
            TEST_PERMISSION_EMAIL_ADDRESS,
            OmhPermissionRole.WRITER
        )

        // Assert
        verify {
            apiService.updateFilePermissions(
                TEST_FILE_ID,
                emailMemberSelector,
                AccessLevel.EDITOR
            )
        }
    }

    @Test
    fun `scenario when user is personal account`() {
        spaceUsage.setUpMockForPersonalAccount()
        every { apiService.getSpaceUsage() } returns spaceUsage

        assertEquals(100L, repository.getStorageUsage())
        assertEquals(104857600L, repository.getStorageQuota())
    }

    @Test
    fun `scenario when user is team account`() {
        spaceUsage.setupMockForTeamAccount()
        every { apiService.getSpaceUsage() } returns spaceUsage

        assertEquals(1000L, repository.getStorageUsage())
        assertEquals(1048576000L, repository.getStorageQuota())
    }

    @Test
    fun `scenario when user is OTHER account`() {
        spaceUsage.setupMockForOtherAccount()
        every { apiService.getSpaceUsage() } returns spaceUsage

        assertEquals(10000L, repository.getStorageUsage())
        assertEquals(-1L, repository.getStorageQuota())
    }

    @Test(expected = OmhStorageException.ApiException::class)
    fun `scenario when about request fails, DbxApiException is thrown`() {
        every { apiService.getSpaceUsage() }.throws(dbxApiException)

        repository.getStorageQuota()
        repository.getStorageUsage()
    }

    @Test
    fun `test resolve path of non-existent file`() {
        every { apiService.queryNodeIdHaving(any()) } answers { callOriginal() }
        assertNull(repository.resolvePath("/foo/bar"))

        verify {
            apiService invoke "listFilesAt" withArguments listOf(
                "" // Root
            )
        }
    }

    @Test
    fun `test resolve path of an existing file`() {
        every {
            apiService invoke "listFilesAt" withArguments
                listOf("")
        } returns testQueryRootFolder
        every {
            apiService invoke "listFilesAt" withArguments
                listOf("/RSX")
        } returns testQueryFolderRsx
        every {
            apiService invoke "listFilesAt" withArguments
                listOf("/RSX/1")
        } returns testQueryFolder1
        every {
            apiService invoke "listFilesAt" withArguments
                listOf("/RSX/1/2")
        } returns testQueryFolder2
        every {
            apiService invoke "listFilesAt" withArguments
                listOf("/RSX/1/2/3")
        } returns testQueryFolder3
        every { apiService.getFile("id of file /RSX/1/2/3/testfile.jpg") } returns
            testFileJpg()

        every { apiService.queryNodeIdHaving(any()) } answers { callOriginal() }
        every { metadataToOmhStorageEntity(testFileJpg()) } returns OmhStorageEntity.OmhFile(
            id = "id of file /RSX/1/2/3/testfile.jpg",
            name = "testfile.jpg",
            createdTime = TEST_FILE_MODIFIED_TIME,
            modifiedTime = TEST_FILE_MODIFIED_TIME,
            parentId = "id of folder /RSX/1/2/3",
            mimeType = "image/jpeg",
            extension = "jpg",
            size = 12345
        )

        val result = repository.resolvePath("/RSX/1/2/3/testfile.jpg")
        assertNotNull(result)
        assertEquals("id of file /RSX/1/2/3/testfile.jpg", result?.id)

        verify {
            apiService invoke "listFilesAt" withArguments listOf("")
        }
        verify {
            apiService invoke "listFilesAt" withArguments listOf("/RSX")
        }
        verify {
            apiService invoke "listFilesAt" withArguments listOf("/RSX/1")
        }
        verify {
            apiService invoke "listFilesAt" withArguments listOf("/RSX/1/2")
        }
        verify {
            apiService invoke "listFilesAt" withArguments listOf("/RSX/1/2/3")
        }
        verify { apiService.getFile("id of file /RSX/1/2/3/testfile.jpg") }
    }
}
