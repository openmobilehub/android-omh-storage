package com.openmobilehub.android.storage.plugin.dropbox.restful.data.repository

import com.openmobilehub.android.storage.core.model.OmhCreatePermission
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.model.OmhIdentity
import com.openmobilehub.android.storage.core.model.OmhPermission
import com.openmobilehub.android.storage.core.model.OmhPermissionRole
import com.openmobilehub.android.storage.core.model.OmhStorageEntity
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.core.model.OmhStorageMetadata
import com.openmobilehub.android.storage.core.restful.common.utils.toApiException
import com.openmobilehub.android.storage.core.restful.common.utils.toByteArrayOutputStream
import com.openmobilehub.android.storage.core.utils.escapeUnicode
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxContentApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.retrofit.DropboxRetrofitImpl
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AddFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AddFolderSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AppendUploadSessionRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CheckShareJobStatusRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ContinueRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CreateFileRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CreateFolderRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFolderSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ExportFileRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.FinishUploadSessionRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.GetFileRevisionsRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.GetFileSharingMetadataRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.GetFolderSharingMetadataRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFileSharedMembersRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFolderRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ListFolderSharedMembersRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.NodeMetadataRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.PathRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.SearchFileRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ShareFolderRequestBody
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.UpdateFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.UpdateFolderSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.UploadSessionCursor
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.UploadSessionRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.AddFolderMember
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.groupsToOmhPermissionList
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toMemberSelector
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFile
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFileRevisions
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.toOmhFolder
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.mapper.usersToOmhPermissionList
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FolderMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.GetSpaceUsageResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.NodeMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SearchResultResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ShareFolderResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File

@Suppress(
    "TooManyFunctions",
    "MagicNumber",
    "UnusedPrivateMember",
    "LargeClass",
    "NestedBlockDepth",
    "ThrowsCount",
    "LongMethod"
)

internal class DropboxRestfulFileRepository(
    private val apiService: DropboxApiService,
    private val contentApiService: DropboxContentApiService,
    smallFileLimitInMB: Int = 150,
) {
    private val smallFileLimit = smallFileLimitInMB * ONE_MEGABYTE

    companion object {
        private const val ONE_MEGABYTE = 1024 * 1024
        private const val APPLICATION_OCTET_STREAM = "application/octet-stream"
        private val APPLICATION_OCTET_STREAM_MEDIA_TYPE =
            requireNotNull(APPLICATION_OCTET_STREAM.toMediaTypeOrNull())
        private val EMPTY_BYTE_ARRAY = "".toByteArray()

        // Share job polling constants
        private const val SHARE_JOB_INITIAL_DELAY_MS = 500L
        private const val SHARE_JOB_MAX_DELAY_MS = 5000L
        private const val SHARE_JOB_MAX_ATTEMPTS = 30
        private const val SHARE_JOB_BACKOFF_MULTIPLIER = 1.5

        @JvmStatic
        private val logger: Logger =
            LoggerFactory.getLogger(DropboxRestfulFileRepository::class.java)
    }

    suspend fun createFolder(
        name: String,
        parentId: String?,
    ): OmhStorageEntity.OmhFolder? {
        val parentFolder =
            if (!parentId.isNullOrEmpty()) {
                getNodeMetadata(path = parentId)
            } else {
                null
            }
        return apiService.createFolder(
            CreateFolderRequest(
                path = "${parentFolder?.path ?: ""}/$name"
            ),
        ).body()?.metadata?.toOmhFolder(parentFolder?.id ?: "")
    }

    suspend fun createFile(
        name: String,
        parentId: String?,
    ): OmhStorageEntity.OmhFile? {
        return uploadSmallFile(
            name,
            EMPTY_BYTE_ARRAY.toRequestBody(
                APPLICATION_OCTET_STREAM_MEDIA_TYPE
            ),
            parentId
        )
    }

    suspend fun deleteFile(fileId: String): Boolean {
        apiService.deleteFile(PathRequestBody(fileId))
        return true
    }

    suspend fun permanentlyDeleteFile(fileId: String): Boolean {
        val response = apiService.permanentlyDeleteFile(PathRequestBody(fileId))
        return if (response.isSuccessful) {
            true
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getFileRevisions(
        fileId: String,
    ): List<OmhFileVersion> {
        // Dropbox API requires the file path to get revisions
        // so we need to fetch the metadata first
        val nodeMetadata = getNodeMetadata(id = fileId)
        if (nodeMetadata != null) {
            val response = apiService.getFileRevisionList(
                GetFileRevisionsRequest(path = nodeMetadata.path, mode = "path")
            )

            return if (response.isSuccessful) {
                response.body()!!.toOmhFileRevisions()
            } else {
                throw response.toApiException()
            }
        } else {
            return emptyList()
        }
    }

    @Suppress("MagicNumber")
    suspend fun downloadFile(
        fileId: String
    ): ByteArrayOutputStream {
        val downloadResponse = contentApiService.downloadFile(serialize(PathRequestBody(fileId)))

        if (downloadResponse.isSuccessful) {
            return downloadResponse.body().toByteArrayOutputStream()
        } else {
            throw downloadResponse.toApiException()
        }
    }

    @Suppress("UnusedPrivateMember")
    suspend fun downloadFileVersion(
        fileId: String, // File ID is not used for Dropbox
        versionId: String,
    ): ByteArrayOutputStream {
        val response = contentApiService.downloadFile(serialize(PathRequestBody("rev:$versionId")))

        return if (response.isSuccessful) {
            response.body().toByteArrayOutputStream()
        } else {
            throw response.toApiException()
        }
    }

    /**
     * Implemented as best effort only. Unused for the time being
     */
    @Suppress("MagicNumber")
    suspend fun exportFile(
        fileId: String,
        exportedMimeType: String,
    ): ByteArrayOutputStream {
        val fileMetaData = getNodeMetadata(id = fileId)

        return fileMetaData?.run {
            require(fileMetaData is FileMetadata)
            if (false == fileMetaData.exportInfo?.exportOptions?.contains(exportedMimeType)) {
                throw IllegalArgumentException(
                    "Requested type $exportedMimeType not supported by file" +
                        " Supported types: " +
                        fileMetaData.exportInfo.exportOptions.joinToString(",")
                )
            }
            val response = contentApiService.exportFile(
                serialize(ExportFileRequest(fileId, exportedMimeType))
            )
            if (response.isSuccessful) {
                response.body().toByteArrayOutputStream()
            } else {
                throw response.toApiException()
            }
        } ?: throw OmhStorageException.ApiException(
            404,
            "File not found",
            null,
        )
    }

    suspend fun getFilesList(parentId: String): List<OmhStorageEntity> {
        val retval = mutableListOf<OmhStorageEntity>()
        var result =
            apiService.getFilesList(
                ListFolderRequestBody(
                    path = parentId.ifEmpty { "" },
                ),
            ).body()
        var hasMore = result?.hasMore
        var cursor = result?.cursor
        result?.entries?.let { list ->
            retval.addAll(
                list.map { nodeMetaData ->
                    when (nodeMetaData) {
                        is FileMetadata ->
                            nodeMetaData.toOmhFile(parentId)
                        is FolderMetadata ->
                            nodeMetaData.toOmhFolder(parentId)
                    }
                }
            )
        }
        while (true == hasMore && cursor != null) {
            result =
                apiService.continueGetFilesList(
                    ContinueRequest(cursor),
                ).body()
            hasMore = result?.hasMore
            cursor = result?.cursor
            result?.entries?.let { list ->
                retval.addAll(
                    list.map { nodeMetaData ->
                        when (nodeMetaData) {
                            is FileMetadata ->
                                nodeMetaData.toOmhFile(parentId)
                            is FolderMetadata ->
                                nodeMetaData.toOmhFolder(parentId)
                        }
                    }
                )
            }
        }
        return retval
    }

    suspend fun updateFile(
        localFileToUpload: File,
        fileId: String,
    ): OmhStorageEntity.OmhFile? {
        val file = getNodeMetadata(id = fileId) as? FileMetadata
        val folder = getNodeMetadata(path = file?.path?.substringBeforeLast('/'))
        val parentId = folder?.id ?: ""
        return if (localFileToUpload.length() < smallFileLimit) {
            uploadSmallFile(
                localFileToUpload.name,
                localFileToUpload.asRequestBody(contentType = APPLICATION_OCTET_STREAM_MEDIA_TYPE),
                parentId,
                createMode = CreateFileRequestBody.MODE_OVERWRITE,
            )
        } else {
            uploadBigFile(localFileToUpload, parentId, mode = CreateFileRequestBody.MODE_OVERWRITE)
        }
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

    suspend fun getTemporaryLink(
        fileId: String,
    ): String? {
        return when (val fileMetaData = getNodeMetadata(id = fileId)) {
            is FileMetadata -> apiService.getFileSharingMetadata(
                GetFileSharingMetadataRequestBody(fileMetaData.id)
            ).body()?.previewUrl
            is FolderMetadata -> if (fileMetaData.sharingInfo != null &&
                (false == fileMetaData.sharingInfo.sharedFolderId?.isEmpty())
            ) {
                apiService.getFolderSharingMetadata(
                    GetFolderSharingMetadataRequestBody(fileMetaData.sharingInfo.sharedFolderId)
                ).body()?.previewUrl
            } else {
                null
            }
            else -> null
        }
    }

    suspend fun getSpaceUsage(): GetSpaceUsageResponse {
        val response = apiService.getSpaceUsage()

        return if (response.isSuccessful) {
            response.body()!!
        } else {
            throw response.toApiException()
        }
    }

    suspend fun getFileMetadata(
        fileId: String,
    ): OmhStorageMetadata? {
        val jsonString = getNodeMetadataRaw(id = fileId)
        return if (jsonString != null) {
            val json = JSONObject(jsonString)
            val objectMapper = DropboxRetrofitImpl.objectMapper
            when (json.getString(NodeMetadata.ATTR_TAG)) {
                NodeMetadata.TAG_FILE -> {
                    val fileMetadata: FileMetadata =
                        objectMapper.readerFor(FileMetadata::class.java).readValue(jsonString)
                    OmhStorageMetadata(
                        entity = fileMetadata.toOmhFile(parentId = ""),
                        originalMetadata = json
                    )
                }
                NodeMetadata.TAG_FOLDER -> {
                    val folderMetadata: FolderMetadata =
                        objectMapper.readerFor(FolderMetadata::class.java).readValue(jsonString)
                    OmhStorageMetadata(
                        entity = folderMetadata.toOmhFolder(parentId = ""),
                        originalMetadata = json
                    )
                }
                else -> null
            }
        } else {
            null
        }
    }

    suspend fun getNodeMetadata(
        id: String? = null,
        path: String? = null,
    ): NodeMetadata? {
        val response = getNodeMetadataRaw(id, path)
        if (response == null) {
            return null
        } else {
            val json = JSONObject(response)
            return jsonToNodeMetadata(json)
        }
    }

    suspend fun search(
        query: String,
    ): SearchResultResponse {
        val matchesList = mutableListOf<NodeMetadata>()
        var response = searchRaw(SearchFileRequest(query = query))
        var hasMore = response.optBoolean("has_more", false)
        var cursor = response.optString("cursor", "")

        // Add initial matches
        if (response.has("matches")) {
            val matches = response.getJSONArray("matches")
            for (i in 0 until matches.length()) {
                val item = matches.getJSONObject(i)
                matchesList.add(jsonToNodeMetadataForSearchResult(item))
            }
        } else {
            throw OmhStorageException.ApiException(
                400,
                "Invalid search response",
                null,
            )
        }

        // Continue fetching while has_more is true and cursor is present
        while (hasMore && cursor.isNotEmpty()) {
            response = JSONObject(
                apiService.continueSearch(
                    SearchFileRequest.SearchByCursor(cursor)
                ).body()?.string() ?: "{}"
            )
            val matches = response.optJSONArray("matches")
            if (matches != null) {
                for (i in 0 until matches.length()) {
                    val item = matches.getJSONObject(i)
                    matchesList.add(jsonToNodeMetadataForSearchResult(item))
                }
            }
            hasMore = response.optBoolean("has_more", false)
            cursor = response.optString("cursor", "")
        }

        return SearchResultResponse(matches = matchesList)
    }

    suspend fun getNodePermission(id: String): List<OmhPermission> {
        val node = getNodeMetadata(id = id)
        return when (node) {
            is FileMetadata -> getFileSharedMembersInternal(id)
            is FolderMetadata -> {
                if (node.sharedFolderId == null) {
                    // Folder is not yet shared, so no permissions
                    emptyList()
                } else {
                    getFolderSharedMembersInternal(requireNotNull(node.sharedFolderId))
                }
            }
            else -> throw OmhStorageException.ApiException(
                404,
                "Node not found",
                null,
            )
        }
    }

    suspend fun createNodePermission(
        id: String,
        permission: OmhCreatePermission,
        addMessageAsComment: Boolean = false,
        customMessage: String? = null,
        quiet: Boolean = false,
    ): OmhPermission {
        val nodeMetadata = getNodeMetadata(id = id)
        return when (nodeMetadata) {
            is FileMetadata -> {
                val response = apiService.addFileSharedMember(
                    AddFileSharedMemberRequest(
                        members = listOf(
                            (permission as OmhCreatePermission.CreateIdentityPermission)
                                .toMemberSelector(),
                        ),
                        accessLevel = permission.role,
                        addMessageAsComment = addMessageAsComment,
                        customMessage = customMessage,
                        quiet = quiet,
                        fileId = id
                    )
                )
                if (response.isSuccessful) {
                    // Re-fetch permissions and return the created one
                    findCreatedPermissionOrThrow(id, permission)
                } else {
                    throw response.toApiException()
                }
            }
            is FolderMetadata -> {
                val sharedFolderId: String = if (nodeMetadata.sharedFolderId != null) {
                    nodeMetadata.sharedFolderId
                } else {
                    // Need to share the folder first and wait for completion
                    val objectMapper = DropboxRetrofitImpl.objectMapper
                    val job = requireNotNull(apiService.shareFolder(ShareFolderRequestBody(id, true)).body())

                    // Poll job status with retry mechanism
                    val completedResponseString = pollShareJobUntilComplete(job.jobId)
                    val shareFolderResponse: ShareFolderResponse =
                        objectMapper.readerFor(ShareFolderResponse::class.java)
                            .readValue(completedResponseString)
                    shareFolderResponse.sharedFolderId
                }
                val response = apiService.addFolderSharedMember(
                    AddFolderSharedMemberRequest(
                        members = listOf(
                            AddFolderMember(
                                member =
                                (permission as OmhCreatePermission.CreateIdentityPermission)
                                    .toMemberSelector(),
                                accessLevel = permission.role
                            )
                        ),
                        customMessage = customMessage,
                        quiet = quiet,
                        sharedFolderId = sharedFolderId
                    )
                )
                if (response.isSuccessful) {
                    // Re-fetch permissions and return the created one
                    findCreatedPermissionOrThrow(id, permission)
                } else {
                    throw response.toApiException()
                }
            }
            else -> throw OmhStorageException.ApiException(
                404,
                "Node not found",
                null,
            )
        }
    }

    private fun shareJobIsCompleted(response: ResponseBody?): Boolean {
        return (
            response != null && JSONObject(response.string()).let { json ->
                json.has(".tag") && json.getString(".tag") == "complete"
            }
            )
    }

    /**
     * Polls the share job status until completion with exponential backoff.
     *
     * @param jobId The job ID to poll
     * @return The completed response string
     * @throws OmhStorageException.ApiException if job fails or times out
     */
    private suspend fun pollShareJobUntilComplete(jobId: String): String {
        var attempt = 0
        var delayMs = SHARE_JOB_INITIAL_DELAY_MS

        while (attempt < SHARE_JOB_MAX_ATTEMPTS) {
            val response = apiService.checkShareJobStatus(CheckShareJobStatusRequest(jobId))

            if (!response.isSuccessful) {
                throw response.toApiException()
            }

            val responseBody = requireNotNull(response.body())
            val responseString = responseBody.string()
            val json = JSONObject(responseString)

            when {
                json.has(".tag") && json.getString(".tag") == "complete" -> {
                    // Job completed successfully, return the response string
                    logger.debug("Share job $jobId completed after $attempt attempts")
                    return responseString
                }
                json.has(".tag") && json.getString(".tag") == "failed" -> {
                    // Job failed
                    val errorMessage = json.optString("error", "Share job failed")
                    logger.error("Share job $jobId failed: $errorMessage")
                    throw OmhStorageException.ApiException(
                        500,
                        "Share job failed: $errorMessage",
                        null
                    )
                }
                json.has(".tag") && json.getString(".tag") == "in_progress" -> {
                    // Job still in progress, wait and retry
                    logger.debug("Share job $jobId still in progress, attempt ${attempt + 1}/$SHARE_JOB_MAX_ATTEMPTS")
                    delay(delayMs)

                    // Exponential backoff with max delay cap
                    delayMs = minOf(
                        (delayMs * SHARE_JOB_BACKOFF_MULTIPLIER).toLong(),
                        SHARE_JOB_MAX_DELAY_MS
                    )
                    attempt++
                }
                else -> {
                    // Unknown status
                    logger.warn("Unknown share job status for job $jobId: ${json.optString(".tag", "unknown")}")
                    delay(delayMs)
                    delayMs = minOf(
                        (delayMs * SHARE_JOB_BACKOFF_MULTIPLIER).toLong(),
                        SHARE_JOB_MAX_DELAY_MS
                    )
                    attempt++
                }
            }
        }

        // Timeout reached
        logger.error("Share job $jobId timed out after $SHARE_JOB_MAX_ATTEMPTS attempts")
        throw OmhStorageException.ApiException(
            408,
            "Share job timed out after $SHARE_JOB_MAX_ATTEMPTS attempts",
            null
        )
    }

    suspend fun updateNodePermission(
        id: String,
        permissionId: String,
        role: OmhPermissionRole
    ): OmhPermission {
        val metadata = getNodeMetadata(id = id)
        return when (metadata) {
            is FileMetadata -> {
                val response = apiService.updateFileSharedMember(
                    UpdateFileSharedMemberRequest(
                        accessLevel = role,
                        member = permissionId.toMemberSelector(),
                        fileId = id
                    )
                )
                if (response.isSuccessful) {
                    // Re-fetch permissions and return the updated one by permissionId
                    findUpdatedPermissionOrThrow(id, permissionId)
                } else {
                    throw response.toApiException()
                }
            }
            is FolderMetadata -> {
                val sharedFolderId = metadata.sharedFolderId ?: throw OmhStorageException.ApiException(
                    404,
                    "Folder is not shared",
                    null
                )
                val response = apiService.updateFolderSharedMember(
                    UpdateFolderSharedMemberRequest(
                        accessLevel = role,
                        member = permissionId.toMemberSelector(),
                        sharedFolderId = sharedFolderId
                    )
                )
                if (response.isSuccessful) {
                    // Re-fetch permissions and return the updated one by permissionId
                    findUpdatedPermissionOrThrow(id, permissionId)
                } else {
                    throw response.toApiException()
                }
            }
            else -> throw OmhStorageException.ApiException(
                404,
                "Node not found",
                null,
            )
        }
    }

    suspend fun deleteNodePermission(
        id: String,
        permissionId: String,
    ): Boolean {
        val nodeMetadata = getNodeMetadata(id = id)
        return when (nodeMetadata) {
            is FileMetadata -> {
                apiService.deleteFileSharedMembers(
                    DeleteFileSharedMemberRequest(
                        fileId = id,
                        memberId = permissionId
                    )
                )
                true
            }
            is FolderMetadata -> {
                apiService.deleteFolderSharedMembers(
                    DeleteFolderSharedMemberRequest(
                        sharedFolderId = requireNotNull(nodeMetadata.sharedFolderId),
                        memberId = permissionId
                    )
                )
                true
            }
            else -> throw OmhStorageException.ApiException(
                404,
                "Node not found",
                null,
            )
        }
    }

    private fun jsonToNodeMetadata(
        json: JSONObject,
    ): NodeMetadata {
        return jsonToNodeMetadataInternal(
            json,
            json.getString(NodeMetadata.ATTR_TAG)
        )
    }

    private fun jsonToNodeMetadataForSearchResult(
        json: JSONObject,
    ): NodeMetadata {
        val metadata = json.getJSONObject("metadata").getJSONObject("metadata")
        return jsonToNodeMetadataInternal(
            metadata,
            metadata.getString(".tag")
        )
    }

    private fun jsonToNodeMetadataInternal(
        json: JSONObject,
        tag: String
    ): NodeMetadata {
        val objectMapper = DropboxRetrofitImpl.objectMapper
        return when (tag) {
            NodeMetadata.TAG_FILE ->
                objectMapper
                    .readerFor(FileMetadata::class.java)
                    .readValue(json.toString())
            NodeMetadata.TAG_FOLDER ->
                objectMapper
                    .readerFor(FolderMetadata::class.java)
                    .readValue(json.toString())
            else -> throw IllegalArgumentException(
                "Unknown node metadata type: $tag"
            )
        }
    }

    private suspend fun getNodeMetadataRaw(
        id: String? = null,
        path: String? = null,
    ): String? {
        val request =
            if (id != null) {
                NodeMetadataRequest(path = id)
            } else {
                NodeMetadataRequest(path = path ?: "")
            }
        return apiService.getNodeMetaData(request).body()?.string()
    }

    private suspend fun uploadSmallFile(
        filename: String,
        requestBody: RequestBody,
        parentId: String?,
        createMode: String = CreateFileRequestBody.MODE_ADD,
    ): OmhStorageEntity.OmhFile? {
        val parentFolder =
            if (!parentId.isNullOrEmpty()) {
                getNodeMetadata(path = parentId)
            } else {
                null
            }

        val path = "${parentFolder?.path ?: ""}/$filename"

        return contentApiService.createFile(
            serialize(
                CreateFileRequestBody(
                    path = path,
                    mode = createMode
                ),
            ).escapeUnicode(),
            requestBody,
        ).body()?.toOmhFile(parentFolder?.id ?: "")
    }

    private suspend fun uploadBigFile(
        localFileToUpload: File,
        parentId: String?,
        mode: String = CreateFileRequestBody.MODE_ADD,
    ): OmhStorageEntity.OmhFile? {
        val parentFolder =
            if (!parentId.isNullOrEmpty()) {
                getNodeMetadata(path = parentId)
            } else {
                null
            }
        val path = "${parentFolder?.path ?: ""}/${localFileToUpload.name}"

        val session =
            requireNotNull(
                contentApiService.createUploadFileSession(
                    serialize(
                        UploadSessionRequest()
                    ).escapeUnicode()
                ).body(),
            )

        val chunkSize = smallFileLimit
        var bytesRead = 0
        var offset = 0
        val bytes = ByteArray(chunkSize)
        val filebin = localFileToUpload.inputStream()

        while (withContext(Dispatchers.IO) {
            filebin.read(bytes)
        }.also { bytesRead = it } != -1
        ) {
            contentApiService.continueUploadFile(
                serialize(
                    AppendUploadSessionRequest(
                        cursor = UploadSessionCursor(offset.toLong(), session.sessionId),
                    ),
                ),
                bytes.toRequestBody(
                    contentType = APPLICATION_OCTET_STREAM_MEDIA_TYPE,
                    offset = 0,
                    byteCount = bytesRead,
                ),
            )
            offset += bytesRead
            bytes.fill(0)
        }

        val finishUploadResult =
            requireNotNull(
                contentApiService.finishUploadFile(
                    serialize(
                        FinishUploadSessionRequestBody(
                            CreateFileRequestBody(
                                path = path,
                                mode = mode
                            ),
                            UploadSessionCursor(offset.toLong(), session.sessionId),
                        ),
                    ).escapeUnicode(),
                    EMPTY_BYTE_ARRAY.toRequestBody(
                        contentType = APPLICATION_OCTET_STREAM_MEDIA_TYPE
                    ),
                ),
            )

        return finishUploadResult.body()?.toOmhFile(parentFolder?.id ?: "")
    }

    private suspend fun searchRaw(searchRequest: SearchFileRequest): JSONObject {
        val response = apiService.search(searchRequest)

        return if (response.isSuccessful) {
            JSONObject(requireNotNull(response.body()?.string()))
        } else {
            throw response.toApiException()
        }
    }

    @Suppress("StringLiteralDuplication")
    private suspend fun getFileSharedMembersInternal(
        id: String,
    ): List<OmhPermission> {
        var response = apiService.listFileSharedMembers(ListFileSharedMembersRequest(id))
        if (!response.isSuccessful) {
            throw response.toApiException()
        } else {
            val retval = mutableListOf<OmhPermission>()

            var responseJson = JSONObject(response.body()?.string() ?: "{}")
            if (!responseJson.has("groups")) {
                logger.error("Invalid response from Dropbox API")
                if (logger.isDebugEnabled) {
                    logger.debug("Response: $responseJson")
                }
                throw OmhStorageException.ApiException(
                    400,
                    "Invalid response from Dropbox API",
                    null,
                )
            } else {
                var groups = responseJson.getJSONArray("groups").groupsToOmhPermissionList()
                retval.addAll(groups)
                var users = responseJson.getJSONArray("users").usersToOmhPermissionList()
                retval.addAll(users)

                var cursor = responseJson.optString("cursor", "")

                while (!cursor.isNullOrEmpty()) {
                    response = apiService.continueListFileSharedMembers(
                        ContinueRequest(cursor)
                    )
                    responseJson = JSONObject(response.body()?.string() ?: "{}")

                    if (!responseJson.has("groups")) {
                        logger.error("Invalid response from Dropbox API")
                        if (logger.isDebugEnabled) {
                            logger.debug("Response: $responseJson")
                        }
                        return retval
                    } else {
                        cursor = responseJson.optString("cursor", "")
                        groups = responseJson.getJSONArray("groups").groupsToOmhPermissionList()
                        retval.addAll(groups)
                        users = responseJson.getJSONArray("users").usersToOmhPermissionList()
                        retval.addAll(users)
                    }
                }

                return retval
            }
        }
    }

    private suspend fun getFolderSharedMembersInternal(
        id: String,
    ): List<OmhPermission> {
        var response = apiService.listFolderSharedMembers(ListFolderSharedMembersRequest(id))
        if (!response.isSuccessful) {
            throw response.toApiException()
        } else {
            val retval = mutableListOf<OmhPermission>()

            var responseJson = JSONObject(response.body()?.string() ?: "{}")
            if (!responseJson.has("groups")) {
                logger.error("Invalid response from Dropbox API")
                if (logger.isDebugEnabled) {
                    logger.debug("Response: $responseJson")
                }
                throw OmhStorageException.ApiException(
                    400,
                    "Invalid response from Dropbox API",
                    null,
                )
            } else {
                var groups = responseJson.getJSONArray("groups").groupsToOmhPermissionList()
                retval.addAll(groups)
                var users = responseJson.getJSONArray("users").usersToOmhPermissionList()
                retval.addAll(users)

                var cursor = responseJson.optString("cursor", "")

                while (!cursor.isNullOrEmpty()) {
                    response = apiService.continueListFolderSharedMembers(
                        ContinueRequest(cursor)
                    )
                    responseJson = JSONObject(response.body()?.string() ?: "{}")

                    if (!responseJson.has("groups")) {
                        logger.error("Invalid response from Dropbox API")
                        if (logger.isDebugEnabled) {
                            logger.debug("Response: $responseJson")
                        }
                        return retval
                    } else {
                        cursor = responseJson.optString("cursor", "")
                        groups = responseJson.getJSONArray("groups").groupsToOmhPermissionList()
                        retval.addAll(groups)
                        users = responseJson.getJSONArray("users").usersToOmhPermissionList()
                        retval.addAll(users)
                    }
                }

                return retval
            }
        }
    }

    // Helper to locate the created permission in the refreshed list
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
                                is com.openmobilehub.android.storage.core.model.OmhPermissionRecipient.User ->
                                    (perm.identity as? OmhIdentity.User)
                                        ?.emailAddress?.equals(recipient.emailAddress, ignoreCase = true) == true
                                is com.openmobilehub.android.storage.core.model.OmhPermissionRecipient.WithObjectId -> {
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

    private fun serialize(obj: Any): String =
        DropboxRetrofitImpl.objectMapper.writeValueAsString(obj)
}
