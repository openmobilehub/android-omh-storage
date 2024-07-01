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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.permissions.model

import androidx.annotation.StringRes

sealed class FilePermissionsViewAction {
    data class ShowToast(@StringRes val message: Int) : FilePermissionsViewAction()

    data class ShowErrorDialog(@StringRes val title: Int, val message: String) :
        FilePermissionsViewAction()

    object ShowEditView : FilePermissionsViewAction()

    data class CopyUrlToClipboard(val shareUrl: String) : FilePermissionsViewAction()
}