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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.core.model.OmhFileVersion
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileVersionsBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import com.openmobilehub.android.storage.sample.presentation.file_viewer.model.FileViewerViewEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileVersionsDialog : BottomSheetDialogFragment(), FileVersionAdapter.ItemListener {

    private val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    private val versionsViewModel: FileVersionsViewModel by viewModels()
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

        fileVersionAdapter = FileVersionAdapter(this, versionsViewModel.state.value.versions.size)

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

        versionsViewModel.getFileVersions(file.id)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                versionsViewModel.state.collect {
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
