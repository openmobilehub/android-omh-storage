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

package com.openmobilehub.android.storage.sample.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.openmobilehub.android.storage.sample.presentation.util.displayErrorDialog
import com.openmobilehub.android.storage.sample.presentation.util.displayToast
import com.openmobilehub.android.storage.sample.util.LOG_MESSAGE_STATE
import com.openmobilehub.android.storage.sample.util.TAG_VIEW_UPDATE

abstract class BaseFragment<ViewModel : BaseViewModel<State, Event>, State : ViewState, Event : ViewEvent> :
    Fragment() {

    abstract val viewModel: ViewModel


    interface BaseFragmentListener {

        fun finishApplication()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            Log.i(TAG_VIEW_UPDATE, "$LOG_MESSAGE_STATE${state.getName()}")
            buildState(state)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            displayToast(message)
        }

        viewModel.errorDialogMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                displayErrorDialog(message)
            }
        }
    }

    protected fun finishApplication() {
        val activity = activity as? BaseFragmentListener
        activity?.finishApplication()
    }

    override fun onStart() {
        super.onStart()
        val fragmentActivity: FragmentActivity = activity ?: return
        fragmentActivity.onBackPressedDispatcher.addCallback {
            onBackPressed()
        }
    }

    protected open fun onBackPressed() {
        finishApplication()
    }

    protected abstract fun buildState(state: State)

    protected fun dispatchEvent(event: Event) {
        viewModel.dispatchEvent(event)
    }
}
