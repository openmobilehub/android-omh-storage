package com.openmobilehub.android.storage.plugin.onedrive.restful.data.retrofit

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.core.restful.common.data.repository.StorageAuthenticator
import com.openmobilehub.android.storage.core.restful.common.utils.accessToken
import com.openmobilehub.android.storage.plugin.onedrive.restful.BuildConfig
import com.openmobilehub.android.storage.plugin.onedrive.restful.data.OneDriveApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

@Suppress("UnusedPrivateMember")
internal class OneDriveRetrofitImpl(private val omhAuthClient: OmhAuthClient) {

    val objectMapper: ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    val httpClient: OkHttpClient by lazy {
        createHttpClient(false)
    }

    val apiService: OneDriveApiService =
        Retrofit.Builder()
            .client(createHttpClient())
            .addConverterFactory(createConverterFactory())
            .baseUrl(BuildConfig.MSGRAPH_API_URL)
            .build().create(OneDriveApiService::class.java)

    private fun createHttpClient(includeAuthToken: Boolean = true): OkHttpClient {
        val authenticator = StorageAuthenticator(omhAuthClient)
        val builder =
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().apply {
                        if (BuildConfig.DEBUG) setLevel(HttpLoggingInterceptor.Level.BODY)
                    },
                )
        if (includeAuthToken) {
            builder.addInterceptor { chain ->
                val request = setupRequestInterceptor(chain)
                chain.proceed(request)
            }.authenticator(authenticator)
        }
        return builder.build()
    }

    private fun setupRequestInterceptor(chain: Interceptor.Chain) =
        chain
            .request()
            .newBuilder()
            .addHeader(
                StorageAuthenticator.HEADER_AUTHORIZATION_NAME,
                StorageAuthenticator.BEARER.format(omhAuthClient.accessToken.orEmpty()),
            )
            .build()

    private fun createConverterFactory() =
        JacksonConverterFactory.create(
            ObjectMapper().registerModule(KotlinModule()),
        )
}
