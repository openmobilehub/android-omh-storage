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