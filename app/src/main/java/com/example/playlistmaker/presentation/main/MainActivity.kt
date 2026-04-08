package com.example.playlistmaker.presentation.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.playlistmaker.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val bottomNavigationDivider = findViewById<android.view.View>(R.id.bottomNavigationDivider)

        bottomNavigation.setupWithNavController(navController)

        val topLevelDestinations = setOf(
            R.id.mediaFragment,
            R.id.searchFragment,
            R.id.settingsFragment
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevelDestination = destination.id in topLevelDestinations
            bottomNavigation.isVisible = isTopLevelDestination
            bottomNavigationDivider.isVisible = isTopLevelDestination
        }
    }
}
