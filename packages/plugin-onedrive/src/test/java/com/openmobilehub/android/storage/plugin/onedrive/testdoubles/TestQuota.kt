package com.openmobilehub.android.storage.plugin.onedrive.testdoubles

import com.microsoft.graph.models.Quota
import io.mockk.every

internal fun Quota.setupMock() {
    every { used } returns 100L
    every { total } returns 104857600L
}

/**
 * Graph SDK source code indicates these two return values are @Nullable, hence add a test case
 * to test handling.
 *
 * Downstream method should return -1L
 */
internal fun Quota.setupNullReturnValueMock() {
    every { used } returns null
    every { total } returns null
}
