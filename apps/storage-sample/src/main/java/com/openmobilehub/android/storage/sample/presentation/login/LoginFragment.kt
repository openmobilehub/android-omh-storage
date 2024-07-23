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

package com.openmobilehub.android.storage.sample.presentation.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.FragmentLoginBinding
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.presentation.BaseFragment
import com.openmobilehub.android.storage.sample.presentation.util.displayErrorDialog
import com.openmobilehub.android.storage.sample.presentation.util.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<LoginViewModel, LoginViewState, LoginViewEvent>() {

    override val viewModel: LoginViewModel by viewModels()

    private lateinit var binding: FragmentLoginBinding

    private val loginLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
           navigateTo(R.id.action_login_fragment_to_file_viewer_fragment, null, navOptions {
               popUpTo(R.id.login_fragment) {
                   inclusive = true
               }
           })
        } else {
            val errorMessage = result.data?.getStringExtra("errorMessage").toString()
            displayErrorDialog(getString(R.string.login_error, result.resultCode, errorMessage))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater)

        binding.btnLoginGoogle.setOnClickListener { dispatchEvent(LoginViewEvent.LoginWithGoogleClicked) }
        binding.btnLoginDropbox.setOnClickListener { dispatchEvent(LoginViewEvent.LoginWithDropboxClicked) }
        binding.btnLoginMicrosoft.setOnClickListener { dispatchEvent(LoginViewEvent.LoginWithMicrosoftClicked) }

        return binding.root
    }

    override fun buildState(state: LoginViewState) {
        when (state) {
            is LoginViewState.Initial -> {}
            is LoginViewState.StartLogin -> startLogin(state.storageAuthProvider)
        }
    }

    private fun startLogin(storageAuthProvider: StorageAuthProvider) {
        lifecycleScope.launch(Dispatchers.Default) {
            val intent = viewModel.getLoginIntent(storageAuthProvider)
            loginLauncher.launch(intent)
        }

    }
}
