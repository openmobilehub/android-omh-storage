package com.openmobilehub.android.storage.core.utils

import com.openmobilehub.android.storage.core.OmhStorageClient
import com.openmobilehub.android.storage.core.testdoubles.folder1FileList
import com.openmobilehub.android.storage.core.testdoubles.folder2FileList
import com.openmobilehub.android.storage.core.testdoubles.folder3FileList
import com.openmobilehub.android.storage.core.testdoubles.folder4FileList
import com.openmobilehub.android.storage.core.testdoubles.folder5FileList
import com.openmobilehub.android.storage.core.testdoubles.folder6FileList
import com.openmobilehub.android.storage.core.testdoubles.rootFolderList
import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class OmhStorageClientExtensionsTest {

    @MockK(relaxed = true)
    private lateinit var storageClient: OmhStorageClient

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `Test OmhStorageClient folderSize() extension`() = runTest {
        coEvery { storageClient.listFiles("") } returns rootFolderList
        coEvery { storageClient.listFiles("folder1") } returns folder1FileList
        coEvery { storageClient.listFiles("folder2") } returns folder2FileList
        coEvery { storageClient.listFiles("folder3") } returns folder3FileList
        coEvery { storageClient.listFiles("folder4") } returns folder4FileList
        coEvery { storageClient.listFiles("folder5") } returns folder5FileList
        coEvery { storageClient.listFiles("folder6") } returns folder6FileList

        assertEquals(6, storageClient.folderSize("folder6"))
        assertEquals(6, storageClient.folderSize("folder5"))
        assertEquals(6, storageClient.folderSize("folder4"))
        assertEquals(6, storageClient.folderSize("folder3"))
        assertEquals(6, storageClient.folderSize("folder2"))
        assertEquals(4 + 5, storageClient.folderSize("folder1"))
        assertEquals(1 + 2 + 3 + 4 + 5 + 6, storageClient.folderSize(""))
    }
}
