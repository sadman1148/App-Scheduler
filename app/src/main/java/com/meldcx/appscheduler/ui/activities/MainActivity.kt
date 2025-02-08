package com.meldcx.appscheduler.ui.activities

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.meldcx.appscheduler.R
import com.meldcx.appscheduler.databinding.ActivityMainBinding
import com.meldcx.appscheduler.services.AlarmService
import com.meldcx.appscheduler.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var prefs: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            navigateToHome()
        }

    private val accessibilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            navigateToHome()
        }

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
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener {_, destination, _ ->
            if (destination.id == R.id.splashFragment) {
                supportActionBar?.hide()
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                supportActionBar?.show()
                supportActionBar?.title = destination.label
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
        if (!prefs.contains(Constants.FIRST_LAUNCH_KEY) && !isIgnoringBatteryOptimizations()) {
            prefs.edit().putBoolean(Constants.FIRST_LAUNCH_KEY, true).apply()
            showWhiteListDialog()
        }
        if (!isAccessibilityServiceEnabled(AlarmService::class.java)) {
            showAccessibilityDialog()
        } else {
            navigateToHome()
        }
    }

    private fun isAccessibilityServiceEnabled(service: Class<out AccessibilityService>): Boolean {
        val expectedComponentName = ComponentName(this, service)
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServices)

        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName.equals(expectedComponentName.flattenToString(), ignoreCase = true)) {
                return Settings.Secure.getInt(
                    contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                ) == 1
            }
        }
        return false
    }

    private fun navigateToHome() {
        lifecycleScope.launch {
            delay(1500)
            navController.popBackStack()
            navController.navigate(R.id.homeFragment)
        }
    }

    @SuppressLint("BatteryLife") // permission added in manifest
    private fun requestBatteryOptimization() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = Uri.parse("package:${packageName}")
        batteryOptimizationLauncher.launch(intent)
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
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

    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this).let {
            it.setMessage(getString(R.string.accessibility_instructions))
            it.setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
                accessibilityLauncher.launch(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            it.setCancelable(false)
            it.create().show()
        }
    }

    private fun showWhiteListDialog() {
        AlertDialog.Builder(this).let {
            it.setMessage(getString(R.string.whitelist_instructions))
            it.setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
                requestBatteryOptimization()
            }
            it.setCancelable(false)
            it.create().show()
        }
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