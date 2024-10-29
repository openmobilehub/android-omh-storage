package com.openmobilehub.android.storage.plugin.googledrive.nongms.testdoubles

import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.AboutResponse

internal val aboutResponseWithQuotaImposed = AboutResponse(
    AboutResponse.StorageQuota(
        limit = 104857600,
        usageInDrive = 100,
        usageInTrash = 5,
        usage = 99999
    )
)

internal val aboutResponseWithUnlimitedQuota = AboutResponse(
    AboutResponse.StorageQuota(
        limit = -1L,
        usageInDrive = 100,
        usageInTrash = 4,
        usage = 99998
    )
)
