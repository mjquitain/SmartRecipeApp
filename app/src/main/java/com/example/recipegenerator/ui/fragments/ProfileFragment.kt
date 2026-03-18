package com.example.recipegenerator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.example.recipegenerator.HomeActivity
import com.example.recipegenerator.R
import com.example.recipegenerator.SplashActivity
import com.example.recipegenerator.ui.screens.ProfileScreen
import com.example.recipegenerator.ui.theme.RecipeGeneratorTheme

class ProfileFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val activity = requireActivity() as HomeActivity
        return ComposeView(requireContext()).apply {
            setContent {
                RecipeGeneratorTheme(themeViewModel = activity.themeViewModel, appSettingsViewModel = activity.appSettingsViewModel) {
                    val dummyNavController = rememberNavController()
                    ProfileScreen(
                        profileViewModel = activity.profileViewModel,
                        themeViewModel = activity.themeViewModel,   // ADD THIS
                        onBackClick = { findNavController().popBackStack() },
                        onNotificationsClick = {
                            findNavController().navigate(R.id.action_profile_to_notifications)
                        },
                        onAppSettingsClick = {
                            findNavController().navigate(R.id.action_profile_to_app_settings)
                        },
                        onLogOutClick = {
                            requireActivity()
                                .getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                .edit().clear().apply()
                            val intent = Intent(requireContext(), SplashActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}