package com.openmobilehub.android.storage.plugin.dropbox.restful.data

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AddFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.AddFolderSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CheckShareJobStatusRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.ContinueRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.CreateFolderRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFileSharedMemberRequest
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body.DeleteFolderSharedMemberRequest
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
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.CreateFolderResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.CurrentUserAccountResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.GetSpaceUsageResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFileRevisionsResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponseDeserializer
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ShareJobResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SharedFileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SharedFolderMetadata
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Suppress("TooManyFunctions")
internal interface DropboxApiService {
    companion object {
        private const val FILES = "2/files"
        private const val USERS = "2/users"
        private const val SHARING = "2/sharing"
        private const val CONTINUE = "continue"

        private const val CREATE_FOLDER = "create_folder_v2"
        private const val LIST_FOLDER = "list_folder"
        private const val DELETE = "delete"
        private const val PERMANENT_DELETE = "permanently_delete"

        private const val GET_METADATA = "get_metadata"
        private const val GET_USER_INFO = "get_current_account"
        private const val GET_SPACE_USAGE = "get_space_usage"
        private const val GET_REVISION_LIST = "list_revisions"

        // metadata
        private const val GET_FILE_METADATA = "get_file_metadata"
        private const val GET_FOLDER_METADATA = "get_folder_metadata"

        // Sharing
        private const val SHARE_FOLDER = "share_folder"
        private const val GET_JOB_STATUS = "check_share_job_status"
        private const val ADD_FILE_MEMBER = "add_file_member"
        private const val ADD_FOLDER_MEMBER = "add_folder_member"
        private const val UPDATE_FILE_MEMBER = "update_file_member"
        private const val UPDATE_FOLDER_MEMBER = "update_folder_member"
        private const val LIST_FILE_MEMBERS = "list_file_members"
        private const val LIST_FOLDER_MEMBERS = "list_folder_members"
        private const val DELETE_FILE_MEMBERS = "remove_file_member_2"
        private const val DELETE_FOLDER_MEMBERS = "remove_folder_member"

        // Search
        private const val SEARCH = "search_v2"
        private const val SEARCH_CONTINUE_V2 = "continue_v2"
    }

    @POST("$FILES/$CREATE_FOLDER")
    suspend fun createFolder(
        @Body body: CreateFolderRequest,
    ): Response<CreateFolderResponse>

    @POST("$FILES/$GET_METADATA")
    suspend fun getNodeMetaData(
        @Body body: NodeMetadataRequest,
    ): Response<ResponseBody>

    @POST("$FILES/$LIST_FOLDER")
    @JsonDeserialize(using = ListFolderResponseDeserializer::class)
    suspend fun getFilesList(
        @Body body: ListFolderRequestBody,
    ): Response<ListFolderResponse>

    @POST("$FILES/$GET_REVISION_LIST")
    suspend fun getFileRevisionList(
        @Body body: GetFileRevisionsRequest,
    ): Response<ListFileRevisionsResponse>

    @POST("$FILES/$LIST_FOLDER/continue")
    @JsonDeserialize(using = ListFolderResponseDeserializer::class)
    suspend fun continueGetFilesList(
        @Body body: ContinueRequest,
    ): Response<ListFolderResponse>

    @POST("$FILES/$DELETE")
    suspend fun deleteFile(@Body body: PathRequestBody): Response<FileMetadata>

    @POST("$FILES/$PERMANENT_DELETE")
    suspend fun permanentlyDeleteFile(@Body body: PathRequestBody): Response<Unit>

    @POST("$FILES/$GET_USER_INFO")
    suspend fun getCurrentAccount(): Response<CurrentUserAccountResponse>

    @POST("$USERS/$GET_SPACE_USAGE")
    suspend fun getSpaceUsage(): Response<GetSpaceUsageResponse>

    @POST("$SHARING/$GET_FILE_METADATA")
    suspend fun getFileSharingMetadata(
        @Body body: GetFileSharingMetadataRequestBody,
    ): Response<SharedFileMetadata>

    @POST("$SHARING/$GET_FOLDER_METADATA")
    suspend fun getFolderSharingMetadata(
        @Body body: GetFolderSharingMetadataRequestBody,
    ): Response<SharedFolderMetadata>

    @POST("$SHARING/$LIST_FILE_MEMBERS")
    suspend fun listFileSharedMembers(
        @Body body: ListFileSharedMembersRequest,
    ): Response<ResponseBody>

    @POST("$SHARING/$LIST_FILE_MEMBERS/$CONTINUE")
    suspend fun continueListFileSharedMembers(
        @Body body: ContinueRequest,
    ): Response<ResponseBody>

    @POST("$SHARING/$LIST_FOLDER_MEMBERS")
    suspend fun listFolderSharedMembers(
        @Body body: ListFolderSharedMembersRequest,
    ): Response<ResponseBody>

    @POST("$SHARING/$LIST_FOLDER_MEMBERS/$CONTINUE")
    suspend fun continueListFolderSharedMembers(
        @Body body: ContinueRequest,
    ): Response<ResponseBody>

    @POST("$SHARING/$ADD_FILE_MEMBER")
    suspend fun addFileSharedMember(
        @Body body: AddFileSharedMemberRequest
    ): Response<ResponseBody>

    @POST("$SHARING/$SHARE_FOLDER")
    suspend fun shareFolder(
        @Body body: ShareFolderRequestBody
    ): Response<ShareJobResponse>

    @POST("$SHARING/$GET_JOB_STATUS")
    suspend fun checkShareJobStatus(
        @Body body: CheckShareJobStatusRequest
    ): Response<ResponseBody>

    @POST("$SHARING/$ADD_FOLDER_MEMBER")
    suspend fun addFolderSharedMember(
        @Body body: AddFolderSharedMemberRequest
    ): Response<ResponseBody>

    @POST("$SHARING/$UPDATE_FILE_MEMBER")
    suspend fun updateFileSharedMember(
        @Body body: UpdateFileSharedMemberRequest
    ): Response<ResponseBody>

    @POST("$SHARING/$UPDATE_FOLDER_MEMBER")
    suspend fun updateFolderSharedMember(
        @Body body: UpdateFolderSharedMemberRequest
    ): Response<ResponseBody>

    @POST("$SHARING/$DELETE_FILE_MEMBERS")
    suspend fun deleteFileSharedMembers(
        @Body body: DeleteFileSharedMemberRequest
    ): Response<Unit>

    @POST("$SHARING/$DELETE_FOLDER_MEMBERS")
    suspend fun deleteFolderSharedMembers(
        @Body body: DeleteFolderSharedMemberRequest
    ): Response<Unit>

    @POST("$FILES/$SEARCH")
    suspend fun search(
        @Body body: SearchFileRequest
    ): Response<ResponseBody>

    @POST("$FILES/$SEARCH_CONTINUE_V2")
    suspend fun continueSearch(
        @Body body: SearchFileRequest.SearchByCursor
    ): Response<ResponseBody>
}
