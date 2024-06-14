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

import android.content.Intent
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import com.openmobilehub.android.storage.sample.presentation.BaseViewModel
import com.openmobilehub.android.storage.sample.util.coInitialize
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Provider

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val omhAuthClient: Provider<OmhAuthClient>
) : BaseViewModel<LoginViewState, LoginViewEvent>() {

    override fun getInitialState(): LoginViewState = LoginViewState.Initial

    override fun processEvent(event: LoginViewEvent) {
        when (event) {
            LoginViewEvent.Initialize -> initializeEvent()
            LoginViewEvent.LoginWithDropboxClicked -> loginClickedEvent(StorageAuthProvider.DROPBOX)
            LoginViewEvent.LoginWithGoogleClicked -> loginClickedEvent(StorageAuthProvider.GOOGLE)
            LoginViewEvent.LoginWithMicrosoftClicked -> loginClickedEvent(StorageAuthProvider.MICROSOFT)
        }
    }

    suspend fun getLoginIntent(provider: StorageAuthProvider): Intent {
        val previousProvider = sessionRepository.getStorageAuthProvider()
        sessionRepository.setStorageAuthProvider(provider)

        return omhAuthClient.get().run {
            if (previousProvider != provider) {
                coInitialize()
            }
            getLoginIntent()
        }
    }

    private fun initializeEvent() = Unit

    private fun loginClickedEvent(provider: StorageAuthProvider) {
        setState(LoginViewState.StartLogin(provider))
    }
}
