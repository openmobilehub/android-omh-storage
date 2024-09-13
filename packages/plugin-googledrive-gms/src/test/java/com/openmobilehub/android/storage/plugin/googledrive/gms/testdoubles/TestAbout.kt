package com.openmobilehub.android.storage.plugin.googledrive.gms.testdoubles

import com.google.api.services.drive.model.About
import com.google.api.services.drive.model.About.StorageQuota
import io.mockk.every

fun About.setupQuotaAvailableMock() {
    every { storageQuota } returns StorageQuota().also {
        it.usageInDrive = 100
        it.limit = 104857600
    }
}

fun About.setupQuotaUnlimitedMock() {
    every { storageQuota } returns StorageQuota().also {
        it.usageInDrive = 100
        it.limit = null
    }
}
