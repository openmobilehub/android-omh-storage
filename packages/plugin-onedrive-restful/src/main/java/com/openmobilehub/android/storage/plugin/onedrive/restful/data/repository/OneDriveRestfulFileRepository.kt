package com.openmobilehub.android.storage.plugin.onedrive.restful.data.repository

import com.fasterxml.jackson.databind.ObjectMapper
import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRecipient
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.restful.common.utils.isNotSuccessful
import com.openmobilehub.android.storage.core.restful.common.utils.toApiException
import com.openmobilehub.android.storage.core.restful.common.utils.toByteArrayOutputStream
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService.Companion.APPLICATION_OCTET_STREAM
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService.Companion.DRIVE
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService.Companion.ROOT
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.body.CreateFolderRequestBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOmhPermissions
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOmhStorageEntity
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOneDriveInviteBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.mapper.toOneDriveUpdateBody
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.Drive
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.DriveItem
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.source.response.ListFolderResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.net.URLEncoder
import java.util.WeakHashMap
import kotlin.math.min

@Suppress("TooManyFunctions")
internal class OneDriveRestfulFileRepository(
    private val apiService: OneDriveApiService,
    private val httpClient: OkHttpClient,
    smallFileLimitInMB: Int = 4
) {

    private val objectMapper = ObjectMapper()

    private val downloadFileUrls: MutableMap<String, String> = WeakHashMap()

    private val smallFileLimit = smallFileLimitInMB * ONE_MEGABYTE

    companion object {
        private const val ONE_MEGABYTE = 1024 * 1024
        private const val CONTENT_LENGTH = "Content-Length"
        private const val CONTENT_RANGE = "Content-Range"
        private val APPLICATION_OCTET_STREAM_MEDIA_TYPE =
            APPLICATION_OCTET_STREAM.toMediaType()
        private val EMPTY_BYTE_ARRAY = "".toByteArray()
    }

    suspend fun getFilesList(parentId: String): List<OmhStorageEntity> {
        val result = mutableListOf<OmhStorageEntity>()

        // Helper to fetch a page depending on root vs folder id
        suspend fun fetchPage(skipToken: String?): retrofit2.Response<ListFolderResponse> =
            if (parentId == OneDriveApiService.ROOT) {
                apiService.getRootFileList(skipToken)
            } else {
                apiService.getFolderFileList(folderId = parentId, skipToken = skipToken)
            }

        var prevSkip: String? = null
        var response = fetchPage(skipToken = null)
        if (response.isNotSuccessful || response.body() == null) {
            throw response.toApiException()
        }
        response.body()!!.entries.let { entries ->
            result += entries.map { it.toOmhStorageEntity() }
        }
        var skipToken = response.body()!!.getSkipToken()

        while (!skipToken.isNullOrBlank() && skipToken != prevSkip) {
            prevSkip = skipToken
            response = fetchPage(skipToken)
            if (response.isNotSuccessful || response.body() == null) {
                throw response.toApiException()
            }
            val body = response.body()!!
            if (body.entries.isNotEmpty()) {
                result += body.entries.map { it.toOmhStorageEntity() }
            }
            skipToken = body.getSkipToken()
        }

        return result
    }

    suspend fun search(query: String): List<OmhStorageEntity> {
        val result = mutableListOf<OmhStorageEntity>()
        var prevSkip: String? = null
        var response = apiService.searchInRoot(query, null)
        if (response.isNotSuccessful || response.body() == null) {
            throw response.toApiException()
        }
        response.body()!!.entries.let { entries ->
            result += entries.map { it.toOmhStorageEntity() }
        }
        var skipToken = response.body()!!.getSkipToken()
        while (!skipToken.isNullOrBlank() && skipToken != prevSkip) {
            prevSkip = skipToken
            response = apiService.searchInRoot(query, skipToken)
            if (response.isNotSuccessful || response.body() == null) {
                throw response.toApiException()
            }
            val body = response.body()!!
            if (body.entries.isNotEmpty()) {
                result += body.entries.map { it.toOmhStorageEntity() }
            }
            skipToken = body.getSkipToken()
        }
        return result
    }

    suspend fun createFile(
        name: String,
        parentId: String?,
    ): OmhStorageEntity.OmhFile? {
        return uploadSmallFile(name, EMPTY_BYTE_ARRAY.toRequestBody(null), parentId)
    }

    suspend fun createFolder(name: String, parentId: String): OmhStorageEntity.OmhFolder {
        val createFolderRequestBody = CreateFolderRequestBody(name = name)
        return (
            if (parentId == OneDriveApiService.ROOT) {
                apiService.createFolderFromRoot(createFolderRequestBody)
            } else {
                apiService.createFolder(parentId, createFolderRequestBody)
            }
            ).body()?.toOmhStorageEntity() as OmhStorageEntity.OmhFolder
    }

    suspend fun deleteFile(fileId: String): Boolean {
        val response = apiService.deleteFile(fileId)
        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
        return true
    }

    suspend fun uploadFile(
        localFileToUpload: File,
        parentId: String?,
    ): OmhStorageEntity.OmhFile? {
        return if (localFileToUpload.length() < smallFileLimit) {
            uploadSmallFile(
                localFileToUpload.name,
                localFileToUpload.asRequestBody(contentType = APPLICATION_OCTET_STREAM_MEDIA_TYPE),
                parentId,
            )
        } else {
            uploadBigFile(localFileToUpload, parentId)
        }
    }

    suspend fun updateFile(
        localFileToUpload: File,
        itemId: String,
    ): OmhStorageEntity.OmhFile? {
        return if (localFileToUpload.length() < smallFileLimit) {
            val response = apiService.updateFileContent(
                itemId,
                localFileToUpload.asRequestBody(APPLICATION_OCTET_STREAM_MEDIA_TYPE)
            )
            if (response.isNotSuccessful) throw response.toApiException()
            response.body()?.toOmhStorageEntity() as OmhStorageEntity.OmhFile
        } else {
            updateBigFile(localFileToUpload, itemId)
        }
    }

    suspend fun downloadFile(
        fileId: String,
    ): ByteArrayOutputStream {
        val fileDownloadUrl: String =
            if (downloadFileUrls.containsKey(fileId)) {
                downloadFileUrls[fileId]!!
            } else {
                val node = getNodeMetaDataById(fileId)
                node?.run {
                    val url = this.fileDownloadUrl
                    downloadFileUrls[fileId] = url!!
                    url
                } ?: throw OmhStorageException.DownloadException(
                    cause = FileNotFoundException("File with id $fileId not found")
                )
            }
        val response =
            httpClient.newCall(
                Request.Builder()
                    .get()
                    .url(fileDownloadUrl)
                    .build(),
            ).execute()
        return response.body?.toByteArrayOutputStream()!!
    }

    suspend fun getNodeMetaDataById(id: String): DriveItem? {
        return apiService.getItemById(URLEncoder.encode(id, Charsets.UTF_8.name())).body()
    }

    suspend fun getNodeMetaData(path: String): DriveItem? {
        return apiService.getItemByPath(URLEncoder.encode(path, Charsets.UTF_8.name())).body()
    }

    // Current authenticated user's Onedrive instance should never be null.
    suspend fun getDriveInfo(): Drive = apiService.getDrive().body()!!

    private suspend fun uploadSmallFile(
        filename: String,
        requestBody: RequestBody,
        parentId: String?,
    ): OmhStorageEntity.OmhFile? {
        val path = "${getParentPath(parentId)}/$filename"
        return apiService.uploadFile(
            path,
            requestBody,
        ).body()?.toOmhStorageEntity() as OmhStorageEntity.OmhFile
    }

    private suspend fun uploadBigFile(
        localFileToUpload: File,
        parentId: String?,
    ): OmhStorageEntity.OmhFile? {
        val path = "${getParentPath(parentId)}/${localFileToUpload.name}"
        val session =
            requireNotNull(
                apiService.createUploadSession(path),
            )
        val uploadUrl = requireNotNull(session.body()?.uploadUrl)

        val chunkSize = smallFileLimit
        var bytesRead = 0
        var offset = 0
        val bytes = ByteArray(chunkSize)
        val filebin = localFileToUpload.inputStream()
        lateinit var response: Response

        while (withContext(Dispatchers.IO) {
            filebin.read(bytes)
        }.also { bytesRead = it } != -1
        ) {
            val end = min((offset + bytesRead).toLong(), localFileToUpload.length())
            response =
                httpClient.newCall(
                    Request.Builder()
                        .url(uploadUrl)
                        .header(CONTENT_LENGTH, bytesRead.toString())
                        .header(CONTENT_RANGE, "bytes $offset-${end - 1}/${localFileToUpload.length()}")
                        .put(bytes.toRequestBody(APPLICATION_OCTET_STREAM_MEDIA_TYPE, 0, bytesRead))
                        .build(),
                ).execute()
            offset += bytesRead
            bytes.fill(0)
        }

        // When all parts are uploaded, response is expected to be non-empty JSON string
        return response.body?.run {
            val finishUploadResult = objectMapper.readValue(response.body!!.string(), DriveItem::class.java)
            finishUploadResult.toOmhStorageEntity() as OmhStorageEntity.OmhFile
        }
    }

    private suspend fun updateBigFile(
        localFileToUpload: File,
        itemId: String,
    ): OmhStorageEntity.OmhFile? {
        val session = requireNotNull(apiService.createUploadSessionForItem(itemId))
        val uploadUrl = requireNotNull(session.body()?.uploadUrl)

        val chunkSize = smallFileLimit
        var bytesRead = 0
        var offset = 0
        val bytes = ByteArray(chunkSize)
        val filebin = localFileToUpload.inputStream()
        lateinit var response: Response

        while (withContext(Dispatchers.IO) { filebin.read(bytes) }.also { bytesRead = it } != -1) {
            val end = min((offset + bytesRead).toLong(), localFileToUpload.length())
            response = httpClient.newCall(
                Request.Builder()
                    .url(uploadUrl)
                    .header(CONTENT_LENGTH, bytesRead.toString())
                    .header(CONTENT_RANGE, "bytes $offset-${end - 1}/${localFileToUpload.length()}")
                    .put(bytes.toRequestBody(APPLICATION_OCTET_STREAM_MEDIA_TYPE, 0, bytesRead))
                    .build()
            ).execute()
            offset += bytesRead
            bytes.fill(0)
        }

        return response.body?.run {
            val finishUploadResult = objectMapper.readValue(response.body!!.string(), DriveItem::class.java)
            finishUploadResult.toOmhStorageEntity() as OmhStorageEntity.OmhFile
        }
    }

    private suspend fun getParentPath(parentId: String?): String {
        val parentFolder =
            if (!parentId.isNullOrEmpty()) {
                getNodeMetaDataById(id = parentId)
            } else {
                null
            }
        return if (parentId == ROOT) {
            ""
        } else {
            parentFolder?.path?.substringAfter("/$DRIVE/$ROOT:") ?: ""
        }
    }

    // Versions

    suspend fun getItemVersions(fileId: String): List<OmhFileVersion> {
        val response = apiService.getItemVersions(fileId)
        if (response.isNotSuccessful) throw response.toApiException()
        val body = response.body() ?: return emptyList()
        return body.versions.mapNotNull { v ->
            val id = v.id ?: return@mapNotNull null
            val modified = v.lastModifiedDateTime?.fromRFC3339StringToDate() ?: return@mapNotNull null
            OmhFileVersion(fileId = fileId, versionId = id, lastModified = modified)
        }
    }

    suspend fun downloadVersion(fileId: String, versionId: String): ByteArrayOutputStream {
        val response = apiService.downloadVersionContent(fileId, versionId)
        if (response.isNotSuccessful || response.body() == null) throw response.toApiException()
        return response.body()!!.toByteArrayOutputStream()
    }

    // Permissions

    suspend fun getNodePermission(id: String): List<OmhPermission> {
        val response = apiService.getItemPermissions(id)
        return if (response.isNotSuccessful) {
            throw response.toApiException()
        } else {
            response.body()?.permissions.toOmhPermissions()
        }
    }

    suspend fun createNodePermission(
        id: String,
        permission: OmhCreatePermission,
        sendNotificationEmail: Boolean = true,
        emailMessage: String? = null,
    ): OmhPermission {
        val body = permission.toOneDriveInviteBody(sendNotificationEmail, emailMessage)
        val response = apiService.createPermission(id, body)
        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
        // Prefer created permissions from response
        val created = response.body()?.permissions.toOmhPermissions()
        if (created.isNotEmpty()) return created.first()
        // Fallback: re-fetch and locate
        return findCreatedPermissionOrThrow(id, permission)
    }

    suspend fun updateNodePermission(
        id: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        val response = apiService.updatePermission(id, permissionId, role.toOneDriveUpdateBody())
        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
        val mapped = response.body()?.let { listOf(it) }?.toOmhPermissions()?.firstOrNull()
        return mapped ?: findUpdatedPermissionOrThrow(id, permissionId)
    }

    suspend fun deleteNodePermission(
        id: String,
        permissionId: String,
    ): Boolean {
        val response = apiService.deletePermission(id, permissionId)
        if (response.isNotSuccessful) {
            throw response.toApiException()
        }
        return true
    }

    // Helpers to locate created/updated permissions, similar to Dropbox implementation
    private suspend fun findCreatedPermissionOrThrow(
        id: String,
        permission: OmhCreatePermission,
    ): OmhPermission {
        val permissions = getNodePermission(id)
        val match = permissions.firstOrNull { perm ->
            when (perm) {
                is OmhPermission.IdentityPermission -> {
                    when (permission) {
                        is OmhCreatePermission.CreateIdentityPermission -> {
                            val recipient = permission.recipient
                            when (recipient) {
                                is OmhPermissionRecipient.User ->
                                    (perm.identity as? OmhIdentity.User)
                                        ?.emailAddress?.equals(recipient.emailAddress, ignoreCase = true) == true
                                is OmhPermissionRecipient.Group ->
                                    (perm.identity as? OmhIdentity.Group)
                                        ?.emailAddress?.equals(recipient.emailAddress, ignoreCase = true) == true
                                is OmhPermissionRecipient.WithObjectId -> {
                                    when (val ident = perm.identity) {
                                        is OmhIdentity.User -> ident.id == recipient.id
                                        is OmhIdentity.Group -> ident.id == recipient.id
                                        else -> false
                                    }
                                }
                                else -> false
                            }
                        }
                    }
                }
            }
        }
        return match ?: throw OmhStorageException.ApiException(
            message = "Create succeeded but API failed to return expected permission"
        )
    }

    private suspend fun findUpdatedPermissionOrThrow(
        id: String,
        permissionId: String,
    ): OmhPermission {
        val permissions = getNodePermission(id)
        val match = permissions.firstOrNull { perm ->
            (perm as? OmhPermission.IdentityPermission)?.id == permissionId
        }
        return match ?: throw OmhStorageException.ApiException(
            message = "Updated succeeded but API failed to return expected permission"
        )
    }
}
