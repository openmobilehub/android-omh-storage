package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles

import com.openmobilehub.android.storage.core.model.OmhFileRevision
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate

const val TEST_REVISION_FILE_ID = "123"
const val TEST_REVISION_ID = "456"
val TEST_REVISION_FILE_MODIFIED_TIME = "2023-07-04T03:03:55.397Z".fromRFC3339StringToDate()!!

val testOmhRevision = OmhFileRevision(
    TEST_REVISION_FILE_ID,
    TEST_REVISION_ID,
    TEST_REVISION_FILE_MODIFIED_TIME
)
