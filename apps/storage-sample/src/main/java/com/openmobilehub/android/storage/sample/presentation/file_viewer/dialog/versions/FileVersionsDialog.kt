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

package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileVersionsBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.BaseDialog
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileVersionsDialog : BaseDialog(), FileVersionAdapter.ItemListener {

    override val viewModel: FileVersionsViewModel by viewModels()

    private lateinit var binding: DialogFileVersionsBinding

    private var fileVersionAdapter: FileVersionAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DialogFileVersionsBinding.inflate(
            inflater,
            container,
            false
        ).also {
            binding = it
            setupBinding()
        }.root
    }

    private fun setupBinding() = with(binding) {
        val file = requireNotNull(parentViewModel.lastFileClicked)
        header.title.text = resources.getString(R.string.text_versions)
        header.fileName.text = file.name
    }

    private fun buildLoadingState() = with(binding) {
        progressBar.visibility = View.VISIBLE
        fileVersionsList.visibility = View.GONE
    }

    private fun initializeAdapter(versions: List<OmhFileVersion>) {
        if (fileVersionAdapter != null) {
            return
        }

        fileVersionAdapter = FileVersionAdapter(this, viewModel.state.value.versions.size)

        context?.let { context ->

            with(binding.fileVersionsList) {
                layoutManager = LinearLayoutManager(context)
                adapter = fileVersionAdapter
            }
        }

        fileVersionAdapter?.submitList(versions)
    }

    private fun buildLoadedState(versions: List<OmhFileVersion>) = with(binding) {
        progressBar.visibility = View.GONE
        fileVersionsList.visibility = View.VISIBLE

        initializeAdapter(versions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = requireNotNull(parentViewModel.lastFileClicked)

        viewModel.getFileVersions(file.id)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect {
                    if (it.isLoading) {
                        buildLoadingState()
                    } else {
                        buildLoadedState(it.versions)
                    }
                }
            }
        }
    }

    override fun onFileVersionClicked(version: OmhFileVersion) {
        dismiss()
        parentViewModel.dispatchEvent(FileViewerViewEvent.FileVersionClicked(version))
    }
}
