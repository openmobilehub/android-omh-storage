package com.openmobilehub.android.storage.core.utils

import java.io.File
import java.io.FileInputStream

// Wrapper for File.inputStream()
// Useful for unit testing as it's not possible to mock .inputStream() extension
fun File.toInputStream(): FileInputStream {
    return this.inputStream()
}
