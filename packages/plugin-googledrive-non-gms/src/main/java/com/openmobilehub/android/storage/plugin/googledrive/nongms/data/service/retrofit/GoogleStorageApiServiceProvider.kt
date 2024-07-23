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

package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.retrofit

import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.plugin.googledrive.nongms.BuildConfig
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.GoogleStorageApiService
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.utils.accessToken
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

internal class GoogleStorageApiServiceProvider(private val omhAuthClient: OmhAuthClient) {

    companion object {
        internal const val HEADER_AUTHORIZATION_NAME = "Authorization"
        internal const val BEARER = "Bearer %s"
    }

    fun getGoogleStorageApiService(): GoogleStorageApiService {
        val retrofit = Retrofit.Builder()
            .client(createOkHttpClient())
            .addConverterFactory(createConverterFactory())
            .baseUrl(BuildConfig.G_STORAGE_URL)
            .build()

        return retrofit.create(GoogleStorageApiService::class.java)
    }

    private fun createOkHttpClient(): OkHttpClient {
        val authenticator = StorageAuthenticator(omhAuthClient)
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = setupRequestInterceptor(chain)
                chain.proceed(request)
            }
            .authenticator(authenticator)
            .build()
    }

    private fun setupRequestInterceptor(chain: Interceptor.Chain) = chain
        .request()
        .newBuilder()
        .addHeader(
            HEADER_AUTHORIZATION_NAME,
            BEARER.format(omhAuthClient.accessToken.orEmpty())
        )
        .build()

    private fun createConverterFactory() = JacksonConverterFactory.create()
}
