package com.openmobilehub.android.storage.plugin.dropbox.restful.testdoubles

import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.GetSpaceUsageResponse
import com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.response.SpaceAllocation

internal object TestGetSpaceUsageResponse {

    internal val getSpaceUsageResponseWithQuotaImposed = GetSpaceUsageResponse(
        used = 123456789L,
        allocation = SpaceAllocation(allocated = 987654321L)
    )
}
