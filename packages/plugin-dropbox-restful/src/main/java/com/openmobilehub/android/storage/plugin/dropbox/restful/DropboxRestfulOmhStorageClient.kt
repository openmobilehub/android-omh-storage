package com.openmobilehub.android.storage.plugin.dropbox.restful

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.repository.DropboxRestfulFileRepository
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFolder
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderMetadata
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class DropboxRestfulOmhStorageClient(
    authClient: OmhAuthClient,
    private val fileRepository: DropboxRestfulFileRepository
) : OmhStorageClient(authClient) {

    override val rootFolder: String = Constants.ROOT_FOLDER

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return fileRepository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        return fileRepository.search(query).toListOfOmhStorageEntity()
    }

    override suspend fun createFileWithMimeType(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        throw UnsupportedOperationException(
            "Dropbox does not support creating files with mime types. Use createFileWithExtension instead."
        )
    }

    override suspend fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity? {
        return fileRepository.createFile("$name.$extension", parentId)
    }

    override suspend fun createFolder(name: String, parentId: String): OmhStorageEntity? {
        return fileRepository.createFolder(name, parentId)
    }

    override suspend fun deleteFile(id: String) {
        fileRepository.deleteFile(id)
    }

    override suspend fun permanentlyDeleteFile(id: String) {
        fileRepository.permanentlyDeleteFile(id)
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        return fileRepository.uploadFile(localFileToUpload, parentId)
    }

    override suspend fun downloadFile(fileId: String): ByteArrayOutputStream {
        return fileRepository.downloadFile(fileId)
    }

    /**
     * To align with plugin-dropbox behaviour, although Dropbox does has API for exporting
     * certain files.
     */
    override suspend fun exportFile(
        fileId: String,
        exportedMimeType: String
    ): ByteArrayOutputStream {
        // return fileRepository.exportFile(fileId, exportedMimeType)
        throw UnsupportedOperationException("Exporting files is not supported in Dropbox")
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhStorageEntity? {
        return fileRepository.updateFile(localFileToUpload, fileId)
    }

    override suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return fileRepository.getFileRevisions(fileId)
    }

    override suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream {
        return fileRepository.downloadFileVersion(fileId, versionId)
    }

    override suspend fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        return fileRepository.getFileMetadata(fileId)
    }

    override suspend fun getFilePermissions(fileId: String): List<OmhPermission> {
        return fileRepository.getNodePermission(fileId)
    }

    override suspend fun deletePermission(fileId: String, permissionId: String) {
        fileRepository.deleteNodePermission(fileId, permissionId)
    }

    override suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission? {
        return fileRepository.updateNodePermission(
            id = fileId,
            permissionId = permissionId,
            role = role
        )
    }

    override suspend fun createPermission(
        fileId: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean,
        emailMessage: String?
    ): OmhPermission? {
        return fileRepository.createNodePermission(
            id = fileId,
            permission = permission,
            quiet = !sendNotificationEmail,
            customMessage = emailMessage
        )
    }

    override suspend fun getWebUrl(fileId: String): String? {
        return fileRepository.getTemporaryLink(fileId)
    }

    override suspend fun resolvePath(path: String): OmhStorageEntity? {
        return fileRepository.getNodeMetadata(path = path)?.let { metadata ->
            when (metadata) {
                is FolderMetadata -> metadata.toOmhFolder(rootFolder)
                is FileMetadata -> metadata.toOmhFile(rootFolder)
            }
        }
    }

    override suspend fun getStorageUsage(): Long {
        return fileRepository.getSpaceUsage().used
    }

    override suspend fun getStorageQuota(): Long {
        return fileRepository.getSpaceUsage().allocation.allocated
    }

    override fun getProviderSdk(): Any {
        throw UnsupportedOperationException("Not implemented for Dropbox Restful client")
    }
}
