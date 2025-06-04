package com.openmobilehub.android.storage.plugin.dropbox.restful.data.retrofit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.restful.common.data.repository.StorageAuthenticator
import com.openmobilehub.android.storage.core.restful.common.utils.accessToken
import com.openmobilehub.android.storage.plugin.dropbox.restful.BuildConfig
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.DropboxContentApiService
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.ListFolderResponseDeserializer
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

internal class DropboxRetrofitImpl(private val omhAuthClient: OmhAuthClient) {

    companion object {
        val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())
    }

    val dropboxApiService: DropboxApiService = Retrofit.Builder()
        .client(createOkHttpClient())
        .addConverterFactory(createConverterFactory())
        .baseUrl(BuildConfig.DROPBOX_API_URL)
        .build().create(DropboxApiService::class.java)

    val dropboxContentApiService: DropboxContentApiService = Retrofit.Builder()
        .client(createOkHttpClient())
        .addConverterFactory(createConverterFactory())
        .baseUrl(BuildConfig.DROPBOX_CONTENT_API_URL)
        .build().create(DropboxContentApiService::class.java)

    private fun createOkHttpClient(): OkHttpClient {
        val authenticator = StorageAuthenticator(omhAuthClient)
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = setupRequestInterceptor(chain)
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
                },
            )
            .authenticator(authenticator)
            .build()
    }

    private fun setupRequestInterceptor(chain: Interceptor.Chain) = chain
        .request()
        .newBuilder()
        .addHeader(
            StorageAuthenticator.HEADER_AUTHORIZATION_NAME,
            StorageAuthenticator.BEARER.format(omhAuthClient.accessToken.orEmpty()),
        )
        .build()

    private fun createConverterFactory() = JacksonConverterFactory.create(
        ObjectMapper()
            .registerModule(KotlinModule.Builder().build())
            .registerModule(
                SimpleModule().addDeserializer(
                    ListFolderResponse::class.java,
                    ListFolderResponseDeserializer()
                )
            ),
    )
}
