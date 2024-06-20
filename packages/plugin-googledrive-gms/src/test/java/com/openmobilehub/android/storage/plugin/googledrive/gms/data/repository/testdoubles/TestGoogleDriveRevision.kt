package com.openmobilehub.android.storage.plugin.googledrive.gms.data.repository.testdoubles

import com.google.api.client.util.DateTime
import com.google.api.services.drive.model.Revision
import io.mockk.every

fun Revision.setUpMock() {
    every { id } returns TEST_VERSION_ID
    every { modifiedTime } returns DateTime(TEST_VERSION_FILE_MODIFIED_TIME)
}
