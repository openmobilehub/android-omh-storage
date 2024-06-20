package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles

import com.openmobilehub.android.storage.core.utils.toRFC3339String
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionListRemoteResponse
import com.openmobilehub.android.storage.plugin.googledrive.nongms.data.service.response.RevisionRemoteResponse

internal val testRevisionRemote = RevisionRemoteResponse(
    TEST_VERSION_ID,
    TEST_VERSION_FILE_MODIFIED_TIME.toRFC3339String(),
)

internal val testVersionListRemote = RevisionListRemoteResponse(listOf(testRevisionRemote))
