package com.openmobilehub.android.storage.plugin.dropbox.restful.data

import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.FileMetadata
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.GetTemporaryLinkResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.UploadSessionResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

internal interface DropboxContentApiService {

    companion object {
        private const val FILES = "2/files"

        private const val DROPBOX_API_ARG = "Dropbox-API-Arg"
        private const val CONTENT_TYPE = "Content-Type"

        private const val UPLOAD = "upload"
        private const val UPLOAD_SESSION = "upload_session"
        private const val START = "start"
        private const val APPEND = "append_v2"
        private const val FINISH = "finish"

        private const val DOWNLOAD = "download"

        private const val GET_TEMPORARY_LINK = "get_temporary_link"

        private const val EXPORT = "export"
    }

    @POST("$FILES/$UPLOAD")
    suspend fun createFile(
        @Header(DROPBOX_API_ARG) createRequest: String,
        @Body filePart: RequestBody,
    ): Response<FileMetadata>

    @POST("$FILES/$UPLOAD_SESSION/$START")
    suspend fun createUploadFileSession(
        @Header(DROPBOX_API_ARG) uploadSessionRequest: String,
        @Header(CONTENT_TYPE) contentType: String? = "application/octet-stream",
    ):
        Response<UploadSessionResponse>

    @POST("$FILES/$UPLOAD_SESSION/$APPEND")
    suspend fun continueUploadFile(
        @Header(DROPBOX_API_ARG) appendUploadSessionRequest: String,
        @Body filePart: RequestBody,
    ): Response<Unit>

    @POST("$FILES/$UPLOAD_SESSION/$FINISH")
    suspend fun finishUploadFile(
        @Header(DROPBOX_API_ARG) finishUploadSessionRequestBody: String,
        @Body filePart: RequestBody,
    ): Response<FileMetadata>

    @POST("$FILES/$DOWNLOAD")
    suspend fun downloadFile(@Header(DROPBOX_API_ARG) pathRequestJson: String): Response<ResponseBody>

    @POST("$FILES/$EXPORT")
    suspend fun exportFile(@Header(DROPBOX_API_ARG) exportFileRequestJson: String): Response<ResponseBody>

    @POST("$FILES/$GET_TEMPORARY_LINK")
    suspend fun getTemporaryLink(
        @Header(DROPBOX_API_ARG) pathRequestJson: String
    ): Response<GetTemporaryLinkResponse>
}
