/*
 * Copyright 2023 Open Mobile Hub
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.openmobilehub.android.storage.core.model.OmhStorageException
import com.openmobilehub.android.storage.sample.presentation.file_viewer.SessionDelegate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

abstract class BaseDialogViewModel : ViewModel() {
    lateinit var sessionDelegate: SessionDelegate

    private val _baseAction = Channel<BaseDialogViewAction>()
    val baseAction = _baseAction.receiveAsFlow()

    protected suspend fun handleException(@StringRes title: Int, exception: OmhStorageException) {
        if (sessionDelegate.handleUnauthorized(exception)) {
            return
        }
        exception.cause?.printStackTrace()
        showErrorDialog(title, exception)
    }

    protected suspend fun showErrorDialog(@StringRes title: Int, exception: Exception) {
        _baseAction.send(
            BaseDialogViewAction.ShowErrorDialog(
                title,
                exception.message.orEmpty()
            )
        )
    }
}