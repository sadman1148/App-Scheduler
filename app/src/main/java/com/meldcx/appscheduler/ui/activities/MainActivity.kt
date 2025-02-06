package com.meldcx.appscheduler.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Hide the status bar and navigation bar
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener {_, destination, _ ->
            supportActionBar?.title = destination.label
        }
        with(binding) {
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
            bottomNavigationView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.menu_item_apps -> {
                        if (navController.currentDestination?.id != R.id.homeFragment) {
                            navController.popBackStack()
                            navController.navigate(R.id.homeFragment)
                        }
                        true
                    }
                    R.id.menu_item_schedule -> {
                        if (navController.currentDestination?.id != R.id.scheduleFragment) {
                            navController.popBackStack()
                            navController.navigate(R.id.scheduleFragment)
                        }
                        true
                    }
                    else -> true
                }
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    showExitConfirmation()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.exit -> {
            showExitConfirmation()
            true
        }
        else -> true
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this).let {
            it.setMessage(getString(R.string.exit_confirmation))
            it.setPositiveButton("Yes") { dialog, _ ->
                dialog.cancel()
                finish()
            }
            it.setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            it.create().show()
        }
    }
}