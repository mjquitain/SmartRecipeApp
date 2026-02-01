package com.example.recipegenerator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Define variables for your UI elements
    private lateinit var btnToggleSignIn: Button
    private lateinit var btnToggleSignUp: Button
    private lateinit var btnMainAction: Button
    private lateinit var tvTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var layoutSignInFields: LinearLayout
    private lateinit var layoutSignUpFields: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        btnToggleSignIn = findViewById(R.id.btnToggleSignIn)
        btnToggleSignUp = findViewById(R.id.btnToggleSignUp)
        btnMainAction = findViewById(R.id.btnMainAction)
        tvTitle = findViewById(R.id.Title)
        tvSubTitle = findViewById(R.id.Subtitle)
        layoutSignInFields = findViewById(R.id.layoutSignInFields)
        layoutSignUpFields = findViewById(R.id.layoutSignUpFields)

        // Set initial state
        updateToggleUI(isSignIn = true)

        btnToggleSignIn.setOnClickListener {
            updateToggleUI(isSignIn = true)
        }

        btnToggleSignUp.setOnClickListener {
            updateToggleUI(isSignIn = false)
        }

        // ADD THIS: Navigate to your screens when Sign In/Sign Up is clicked
        btnMainAction.setOnClickListener {
            // Navigate to HomeActivity (your Compose screens)
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            // Optional: finish() if you don't want to come back to login
            // finish()
        }
    }

    private fun updateToggleUI(isSignIn: Boolean) {
        if (isSignIn) {
            // UI Toggle Colors
            btnToggleSignIn.setBackgroundResource(R.drawable.bg_tab_container)
            btnToggleSignIn.setTextColor(Color.WHITE)
            btnToggleSignUp.background = null
            btnToggleSignUp.setTextColor(Color.BLACK)

            // Content Swap
            tvTitle.text = getString(R.string.signin_title)
            tvSubTitle.text = "Ready to cook something good? Let's check your ingredients and find out what you can eat."
            btnMainAction.text = "Sign In"
            layoutSignInFields.visibility = View.VISIBLE
            layoutSignUpFields.visibility = View.GONE
        } else {
            // UI Toggle Colors
            btnToggleSignUp.setBackgroundResource(R.drawable.bg_tab_container)
            btnToggleSignUp.setTextColor(Color.WHITE)
            btnToggleSignIn.background = null
            btnToggleSignIn.setTextColor(Color.BLACK)

            // Content Swap
            tvTitle.text = getString(R.string.signup_title)
            tvSubTitle.text = "Wanna start cooking smarter? Sign up to track ingredients and find safe recipes."
            btnMainAction.text = "Sign Up"
            layoutSignInFields.visibility = View.GONE
            layoutSignUpFields.visibility = View.VISIBLE
        }
    }
}