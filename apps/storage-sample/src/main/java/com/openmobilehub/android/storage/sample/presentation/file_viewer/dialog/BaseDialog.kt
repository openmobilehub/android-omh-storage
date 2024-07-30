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

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.presentation.util.displayErrorDialog
import kotlinx.coroutines.launch

abstract class BaseDialog : BottomSheetDialogFragment() {
    protected val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    protected abstract val viewModel: BaseDialogViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.baseAction.collect(::handleAction)
            }
        }
    }

    private fun setupViewModel() {
        viewModel.sessionDelegate = parentViewModel
    }

    private fun handleAction(action: BaseDialogViewAction) {
        when (action) {
            is BaseDialogViewAction.ShowErrorDialog -> displayErrorDialog(
                action.message,
                action.title
            )
        }
    }
}