package com.openmobilehub.android.storage.sample.presentation.file_viewer.dialog.versions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.openmobilehub.android.storage.core.model.OmhFileRevision
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.DialogFileVersionsBinding
import com.openmobilehub.android.storage.sample.presentation.file_viewer.FileViewerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FileVersionsDialog : BottomSheetDialogFragment(), FileRevisionAdapter.ItemListener {

    private val parentViewModel: FileViewerViewModel by viewModels({ requireParentFragment() })
    private val revisionsViewModel: FileVersionsViewModel by viewModels()
    private lateinit var binding: DialogFileVersionsBinding

    private var fileRevisionAdapter: FileRevisionAdapter? = null

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

    private fun initializeAdapter(revisions: List<OmhFileRevision>) {
        if (fileRevisionAdapter != null) {
            return
        }

        fileRevisionAdapter = FileRevisionAdapter(this)

        context?.let { context ->

            with(binding.fileVersionsList) {
                layoutManager = LinearLayoutManager(context)
                adapter = fileRevisionAdapter
            }
        }

        fileRevisionAdapter?.submitList(revisions)
    }

    private fun buildLoadedState(revisions: List<OmhFileRevision>) = with(binding) {
        Log.v("FileVersionsDialog", "Revisions: $revisions")
        progressBar.visibility = View.GONE
        fileVersionsList.visibility = View.VISIBLE

        initializeAdapter(revisions)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file = requireNotNull(parentViewModel.lastFileClicked)

        revisionsViewModel.getRevisions(file.id)

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                revisionsViewModel.state.collect {
                    if (it.isLoading || it.isDownloading) {
                        buildLoadingState()
                    } else {
                        buildLoadedState(it.revisions)
                        // TODO update UI with the list of revisions
                    }
                    // TODO update UI with the loading status and a list of revisions
                }
            }
        }
    }

    override fun onFileClicked(revision: OmhFileRevision) {
        revisionsViewModel.downloadRevision(revision.fileId, revision.revisionId)
    }
}
