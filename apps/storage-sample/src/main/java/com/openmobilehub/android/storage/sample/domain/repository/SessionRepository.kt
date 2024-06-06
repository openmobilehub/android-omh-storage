package com.openmobilehub.android.storage.sample.domain.repository

import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider

interface SessionRepository {
    suspend fun initialise()
    suspend fun setStorageAuthProvider(provider: StorageAuthProvider)
    fun getStorageAuthProvider(): StorageAuthProvider
}