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

package com.openmobilehub.android.storage.sample.presentation.main_activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.openmobilehub.android.auth.core.OmhAuthClient
import com.openmobilehub.android.storage.sample.R
import com.openmobilehub.android.storage.sample.databinding.ActivityBaseBinding
import com.openmobilehub.android.storage.sample.domain.repository.SessionRepository
import com.openmobilehub.android.storage.sample.presentation.BaseFragment
import com.openmobilehub.android.storage.sample.util.coInitialize
import com.openmobilehub.android.storage.sample.util.validateSession
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
open class MainActivity : AppCompatActivity(), BaseFragment.BaseFragmentListener {

    private val binding: ActivityBaseBinding by lazy {
        ActivityBaseBinding.inflate(layoutInflater)
    }
    private lateinit var navController: NavController

    @Inject
    lateinit var sessionRepository: SessionRepository

    @Inject
    lateinit var omhAuthClient: Provider<OmhAuthClient>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupEdgeToEdgeInsets()
        setSupportActionBar(binding.toolbar)
        setupNavigation()
    }

    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusbarPlaceholder.updateLayoutParams { height = systemBars.top }
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment: NavHostFragment = supportFragmentManager.findFragmentById(
            /* id = */ R.id.activity_base_fragment_container
        ) as NavHostFragment
        navController = navHostFragment.navController
        setupGraph()
        setupToolbar()
    }

    private fun setupToolbar() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.file_viewer_fragment, R.id.login_fragment)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    private fun setupGraph() {
        lifecycleScope.launch(Dispatchers.IO) {
            // We need to initialise sessionRepository before accessing omhAuthClient so the correct
            // storageAuthProvider can be used
            sessionRepository.initialise()
            val isUserLoggedIn = omhAuthClient.get().run {
                coInitialize()
                validateSession()
            }
            launch(Dispatchers.Main) {
                val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
                val startDestId = if (isUserLoggedIn) {
                    R.id.file_viewer_fragment
                } else {
                    R.id.login_fragment
                }
                navGraph.setStartDestination(startDestId)
                navController.graph = navGraph
            }
        }
    }

    override fun finishApplication() {
        finish().also { finishAffinity() }
    }
}