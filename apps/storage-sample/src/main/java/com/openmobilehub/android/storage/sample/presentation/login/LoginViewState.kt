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

import com.openmobilehub.android.storage.sample.domain.model.StorageAuthProvider
import com.openmobilehub.android.storage.sample.presentation.ViewState

sealed class LoginViewState : ViewState {

    object Initial : LoginViewState() {

        override fun getName() = "LoginViewState.Initial"
    }

    data class StartLogin(val storageAuthProvider: StorageAuthProvider) : LoginViewState() {

        override fun getName() = "LoginViewState.StartLogin"
    }
}
