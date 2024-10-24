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

package com.openmobilehub.android.storage.plugin.dropbox.data.service

import com.dropbox.core.v2.async.LaunchResultBase
import com.dropbox.core.v2.files.CreateFolderResult
import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.FolderMetadata
import com.dropbox.core.v2.files.ListFolderResult
import com.dropbox.core.v2.files.ListRevisionsMode
import com.dropbox.core.v2.files.ListRevisionsResult
import com.dropbox.core.v2.files.Metadata
import com.dropbox.core.v2.files.RelocationResult
import com.dropbox.core.v2.files.SearchV2Result
import com.dropbox.core.v2.files.WriteMode
import com.dropbox.core.v2.sharing.AccessInheritance
import com.dropbox.core.v2.sharing.AccessLevel
import com.dropbox.core.v2.sharing.AddMember
import com.dropbox.core.v2.sharing.FileMemberActionResult
import com.dropbox.core.v2.sharing.FileMemberRemoveActionResult
import com.dropbox.core.v2.sharing.MemberAccessLevelResult
import com.dropbox.core.v2.sharing.MemberSelector
import com.dropbox.core.v2.sharing.ShareFolderJobStatus
import com.dropbox.core.v2.sharing.ShareFolderLaunch
import com.dropbox.core.v2.sharing.SharedFileMembers
import com.dropbox.core.v2.sharing.SharedFolderMembers
import com.dropbox.core.v2.users.SpaceUsage
import com.openmobilehub.android.storage.core.utils.splitPathToParts
import java.io.ByteArrayOutputStream
import java.io.InputStream

@Suppress("TooManyFunctions")
internal class DropboxApiService(internal val apiClient: DropboxApiClient) {

    fun getFilesList(parentId: String): ListFolderResult {
        return apiClient.dropboxApiService.files().listFolder(parentId)
    }

    fun uploadFile(
        inputStream: InputStream,
        path: String,
        withAutorename: Boolean = true,
        writeMode: WriteMode = WriteMode.ADD,
        withStrictConflict: Boolean = true
    ): FileMetadata {
        // withAutorename(true) is used to avoid conflicts with existing files
        // by renaming the uploaded file. It matches the Google Drive API behavior.
        // withStrictConflict(true) is used to rename the file even if the content is the same.

        return apiClient.dropboxApiService.files().uploadBuilder(path)
            .withAutorename(withAutorename)
            .withMode(writeMode)
            .withStrictConflict(withStrictConflict)
            .uploadAndFinish(inputStream)
    }

    fun downloadFile(fileId: String, outputStream: ByteArrayOutputStream): FileMetadata {
        return apiClient.dropboxApiService.files().download(fileId).download(outputStream)
    }

    fun getFileRevisions(fileId: String): ListRevisionsResult {
        return apiClient.dropboxApiService.files().listRevisionsBuilder(fileId)
            .withMode(ListRevisionsMode.ID).start()
    }

    fun downloadFileRevision(
        revisionId: String,
        outputStream: ByteArrayOutputStream
    ): FileMetadata {
        val path = "rev:$revisionId"

        return apiClient.dropboxApiService.files().download(path)
            .download(outputStream)
    }

    fun deleteFile(fileId: String): DeleteResult {
        return apiClient.dropboxApiService.files().deleteV2(fileId)
    }

    fun search(query: String): SearchV2Result {
        return apiClient.dropboxApiService.files().searchV2(query)
    }

    fun getFile(fileId: String): Metadata {
        return apiClient.dropboxApiService.files().getMetadata(fileId)
    }

    fun createFolder(path: String): CreateFolderResult {
        return apiClient.dropboxApiService.files().createFolderV2(path)
    }

    fun moveFile(fromPath: String, toPath: String): RelocationResult {
        return apiClient.dropboxApiService.files().moveV2Builder(fromPath, toPath)
            .withAutorename(true).start()
    }

    fun createFilePermission(
        fileId: String,
        memberSelector: MemberSelector,
        accessLevel: AccessLevel,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): List<FileMemberActionResult> {
        return apiClient.dropboxApiService
            .sharing()
            .addFileMemberBuilder(fileId, listOf(memberSelector))
            .withQuiet(!sendNotificationEmail)
            .withAccessLevel(accessLevel)
            .withCustomMessage(emailMessage)
            .start()
    }

    fun shareFolder(
        folderId: String,
    ): ShareFolderLaunch {
        return apiClient.dropboxApiService
            .sharing()
            .shareFolderBuilder(folderId)
            .withAccessInheritance(AccessInheritance.INHERIT) // mimic Google Drive behaviour
            .start()
    }

    fun checkShareFolderJobStatus(
        asyncJobIdValue: String,
    ): ShareFolderJobStatus {
        return apiClient.dropboxApiService
            .sharing()
            .checkShareJobStatus(asyncJobIdValue)
    }

    fun createFolderPermission(
        sharedFolderId: String,
        addMember: AddMember,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ) {
        return apiClient.dropboxApiService
            .sharing()
            .addFolderMemberBuilder(sharedFolderId, listOf(addMember))
            .withQuiet(!sendNotificationEmail)
            .withCustomMessage(emailMessage)
            .start()
    }

    fun getFileWebUrl(fileId: String): String {
        return apiClient.dropboxApiService
            .sharing()
            .getFileMetadata(fileId)
            .previewUrl
    }

    fun getFolderWebUrl(sharedFolderId: String): String {
        return apiClient.dropboxApiService
            .sharing()
            .getFolderMetadata(sharedFolderId)
            .previewUrl
    }

    fun getFilePermissions(fileId: String): SharedFileMembers {
        return apiClient.dropboxApiService
            .sharing()
            .listFileMembersBuilder(fileId)
            .withIncludeInherited(true) // mimic Google Drive behaviour
            .start()
    }

    fun getFolderPermissions(sharedFolderId: String): SharedFolderMembers {
        return apiClient.dropboxApiService
            .sharing()
            .listFolderMembers(sharedFolderId)
    }

    fun deleteFolderPermission(
        sharedFolderId: String,
        memberSelector: MemberSelector
    ): LaunchResultBase {
        return apiClient.dropboxApiService
            .sharing()
            .removeFolderMember(sharedFolderId, memberSelector, false)
    }

    fun deleteFilePermission(
        fileId: String,
        memberSelector: MemberSelector
    ): FileMemberRemoveActionResult {
        return apiClient.dropboxApiService
            .sharing()
            .removeFileMember2(fileId, memberSelector)
    }

    fun updateFolderPermissions(
        sharedFolderId: String,
        memberSelector: MemberSelector,
        accessLevel: AccessLevel,
    ): MemberAccessLevelResult {
        return apiClient.dropboxApiService
            .sharing()
            .updateFolderMember(
                sharedFolderId,
                memberSelector,
                accessLevel
            )
    }

    fun updateFilePermissions(
        fileId: String,
        memberSelector: MemberSelector,
        accessLevel: AccessLevel,
    ): MemberAccessLevelResult {
        return apiClient.dropboxApiService
            .sharing()
            .updateFileMember(
                fileId,
                memberSelector,
                accessLevel
            )
    }

    fun getSpaceUsage(): SpaceUsage {
        return apiClient.dropboxApiService.users().spaceUsage
    }

    fun queryNodeIdHaving(path: String): String? {
        val parts = path.splitPathToParts()

        var currentPath = ""
        var node: Metadata? = null

        parts.forEachIndexed { index, part ->
            val entries = listFilesAt(currentPath).entries
            var found = false

            for (entry in entries) {
                if (entry.name == part && if (index < parts.size - 1) {
                    entry is FolderMetadata
                } else {
                        true
                    }
                ) {
                    currentPath += "/$part"
                    node = entry
                    found = true
                    break
                }
            }

            if (!found) {
                return null
            }
        }

        return (node as? FolderMetadata)?.id ?: (node as FileMetadata).id
    }

    private fun listFilesAt(path: String): ListFolderResult =
        apiClient.dropboxApiService.files().listFolder(path)
}
