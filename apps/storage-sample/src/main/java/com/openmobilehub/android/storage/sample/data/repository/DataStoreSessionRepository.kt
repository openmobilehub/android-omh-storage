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

package com.openmobilehub.android.storage.sample.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreSessionRepository(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    private val storageAuthProviderKey = stringPreferencesKey("storage_auth_provider")

    // In memory value for synchronous access
    private var storageAuthProvider: StorageAuthProvider? = null

    override suspend fun initialise() {
        val storedProvider = dataStore.data.map { preferences ->
            preferences[storageAuthProviderKey]
        }.firstOrNull()

        storageAuthProvider =
            if (storedProvider == null)
                StorageAuthProvider.MICROSOFT
            else
                StorageAuthProvider.valueOf(storedProvider)
    }

    override fun getStorageAuthProvider(): StorageAuthProvider {
        return storageAuthProvider
            ?: error("Repository is not initialised")
    }

    override suspend fun setStorageAuthProvider(provider: StorageAuthProvider) {
        dataStore.edit { preferences ->
            preferences[storageAuthProviderKey] = provider.toString()
        }
        storageAuthProvider = provider
    }
}