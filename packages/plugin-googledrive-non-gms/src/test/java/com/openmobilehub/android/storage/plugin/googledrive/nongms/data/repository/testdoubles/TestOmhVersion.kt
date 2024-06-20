package com.openmobilehub.android.storage.plugin.googledrive.nongms.data.repository.testdoubles

import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.core.utils.fromRFC3339StringToDate

const val TEST_VERSION_FILE_ID = "123"
const val TEST_VERSION_ID = "456"
val TEST_VERSION_FILE_MODIFIED_TIME = "2023-07-04T03:03:55.397Z".fromRFC3339StringToDate()!!

val testOmhVersion = OmhFileVersion(
    TEST_VERSION_FILE_ID,
    TEST_VERSION_ID,
    TEST_VERSION_FILE_MODIFIED_TIME
)
