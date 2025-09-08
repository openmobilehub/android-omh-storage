package com.openmobilehub.android.storage.plugin.onedrive.restful

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository.OneDriveRestfulFileRepository
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOmhStorageMetadata
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress("TooManyFunctions")
internal class OneDriveRestfulOmhStorageClientImpl(
    authClient: OmhAuthClient,
    private val repository: OneDriveRestfulFileRepository
) : OmhStorageClient(authClient) {

    override val rootFolder: String = OneDriveApiService.ROOT

    override suspend fun listFiles(parentId: String): List<OmhStorageEntity> {
        return repository.getFilesList(parentId)
    }

    override suspend fun search(query: String): List<OmhStorageEntity> {
        return repository.search(query)
    }

    override suspend fun createFileWithMimeType(
        name: String,
        mimeType: String,
        parentId: String
    ): OmhStorageEntity? {
        throw UnsupportedOperationException(
            "OneDrive does not support creating files with mime type. Use createFileWithExtension instead."
        )
    }

    override suspend fun createFileWithExtension(
        name: String,
        extension: String,
        parentId: String
    ): OmhStorageEntity? {
        val filename = "$name.$extension"
        return repository.createFile(filename, parentId)
    }

    override suspend fun createFolder(name: String, parentId: String): OmhStorageEntity? {
        return repository.createFolder(name, parentId)
    }

    override suspend fun deleteFile(id: String) {
        repository.deleteFile(id)
    }

    override suspend fun permanentlyDeleteFile(id: String) {
        throw UnsupportedOperationException()
    }

    override suspend fun uploadFile(localFileToUpload: File, parentId: String?): OmhStorageEntity? {
        return repository.uploadFile(localFileToUpload, parentId)
    }

    override suspend fun downloadFile(fileId: String): ByteArrayOutputStream {
        return repository.downloadFile(fileId)
    }

    override suspend fun exportFile(fileId: String, exportedMimeType: String): ByteArrayOutputStream {
        throw UnsupportedOperationException("Exporting files is not supported in OneDrive.")
    }

    override suspend fun updateFile(localFileToUpload: File, fileId: String): OmhStorageEntity? {
        return repository.updateFile(localFileToUpload, fileId)
    }

    override suspend fun getFileVersions(fileId: String): List<OmhFileVersion> {
        return repository.getItemVersions(fileId)
    }

    override suspend fun downloadFileVersion(
        fileId: String,
        versionId: String
    ): ByteArrayOutputStream {
        return repository.downloadVersion(fileId, versionId)
    }

    override suspend fun getFileMetadata(fileId: String): OmhStorageMetadata? {
        return repository.getNodeMetaDataById(fileId)?.toOmhStorageMetadata()
    }

    override suspend fun getFilePermissions(fileId: String): List<OmhPermission> {
        return repository.getNodePermission(fileId)
    }

    override suspend fun deletePermission(fileId: String, permissionId: String) {
        repository.deleteNodePermission(fileId, permissionId)
    }

    override suspend fun updatePermission(
        fileId: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission? {
        return repository.updateNodePermission(
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
        return repository.createNodePermission(
            id = fileId,
            permission = permission,
            sendNotificationEmail = sendNotificationEmail,
            emailMessage = emailMessage
        )
    }

    override suspend fun getWebUrl(fileId: String): String? {
        return repository.getNodeMetaDataById(fileId)?.webUrl
    }

    override suspend fun resolvePath(path: String): OmhStorageEntity? {
        return repository.getNodeMetaData(path)?.toOmhStorageEntity()
    }

    override suspend fun getStorageUsage(): Long {
        return repository.getDriveInfo().quota.used
    }

    override suspend fun getStorageQuota(): Long {
        return repository.getDriveInfo().quota.total
    }

    override fun getProviderSdk(): Any {
        throw UnsupportedOperationException("Not implemented for Onedrive Restful client")
    }
}
