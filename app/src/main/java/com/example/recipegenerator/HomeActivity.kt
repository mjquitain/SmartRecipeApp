package com.example.recipegenerator

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.recipegenerator.databinding.ActivityHomeBinding
import com.example.recipegenerator.ui.viewmodel.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val app get() = application as RecipeApp

    // Reads userId once — shared by ingredientViewModel and notification sync
    private val userId: String by lazy {
        getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            .getString("current_username", "") ?: ""
    }

    val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(app.appSettingsRepository)
    }

    val appSettingsViewModel: AppSettingsViewModel by viewModels {
        AppSettingsViewModelFactory(app.appSettingsRepository)
    }

    val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModelFactory(app.recipeRepository)
    }

    val ingredientViewModel: IngredientViewModel by viewModels {
        IngredientViewModelFactory(app.ingredientRepository, userId)
    }

    val notificationViewModel: NotificationViewModel by viewModels {
        NotificationViewModelFactory(app.notificationRepository)
    }

    val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(
            userDao = app.userDao,
            sharedPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationViewModel.syncNotifications(userId)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.recipeDetailFragment,
                R.id.profileFragment,
                R.id.notificationsFragment,
                R.id.appSettingsFragment -> binding.bottomNav.visibility = View.GONE
                else -> binding.bottomNav.visibility = View.VISIBLE
            }
        }
    }
}