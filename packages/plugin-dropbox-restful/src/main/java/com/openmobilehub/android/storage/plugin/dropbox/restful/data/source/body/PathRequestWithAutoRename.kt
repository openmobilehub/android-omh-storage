package com.openmobilehub.android.storage.plugin.dropbox.restful.data.source.body

import androidx.annotation.Keep

@Keep
internal data class PathRequestWithAutoRename(
    val path: String,
    val autorename: Boolean
)
