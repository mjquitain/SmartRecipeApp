package com.example.recipegenerator

import kotlinx.coroutines.flow.first

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

// Room Test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.recipegenerator.data.AppDatabase
import com.example.recipegenerator.data.entity.IngredientEntity

class MainActivity : AppCompatActivity() {

    // Define variables for your UI elements
    private lateinit var btnToggleSignIn: Button
    private lateinit var btnToggleSignUp: Button
    private lateinit var btnMainAction: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var layoutSignInFields: LinearLayout
    private lateinit var layoutSignUpFields: LinearLayout
    private lateinit var etSignInUser: TextInputEditText
    private lateinit var etSignInPass: TextInputEditText
    private lateinit var cbRememberMe: CheckBox
    private lateinit var btnForgot: Button
    private lateinit var btnSignInGoogle: Button
    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etUserSignUp: TextInputEditText
    private lateinit var etEmailSignUp: TextInputEditText
    private lateinit var etPassSignUp: TextInputEditText
    private lateinit var etPassConfirmSignUp: TextInputEditText
    private var isSignInMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Room Test Connection
        val db = AppDatabase.getDatabase(this)

        CoroutineScope(Dispatchers.IO).launch {
            // INSERT
            db.ingredientDao().insertIngredient(
                IngredientEntity(
                    name = "Tomato",
                    category = "Vegetable",
                    quantity = "3",
                    unit = "pcs",
                    expirationDate = "2025-03-01"
                )
            )

            // READ
            val list = db.ingredientDao().getAllIngredients().first()
                list.forEach {
                    android.util.Log.d("RoomTest", "Ingredient: ${it.name}, qty: ${it.quantity}")
                }
            }

        // Initialize views
        btnToggleSignIn = findViewById(R.id.btnToggleSignIn)
        btnToggleSignUp = findViewById(R.id.btnToggleSignUp)
        btnMainAction = findViewById(R.id.btnMainAction)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.Title)
        tvSubTitle = findViewById(R.id.Subtitle)
        layoutSignInFields = findViewById(R.id.layoutSignInFields)
        layoutSignUpFields = findViewById(R.id.layoutSignUpFields)
        etSignInUser = findViewById(R.id.etSignInUser)
        etSignInPass = findViewById(R.id.etSignInPass)
        cbRememberMe = findViewById(R.id.cbRememberMe)
        btnForgot = findViewById(R.id.btnForgot)
        btnSignInGoogle = findViewById(R.id.btnSignInGoogle)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etUserSignUp = findViewById(R.id.etUserSignUp)
        etEmailSignUp = findViewById(R.id.etEmailSignUp)
        etPassSignUp = findViewById(R.id.etPassSignUp)
        etPassConfirmSignUp = findViewById(R.id.etPassConfirmSignUp)

        // Set initial state
        updateToggleUI(isSignIn = true)

        btnToggleSignIn.setOnClickListener {
            updateToggleUI(isSignIn = true)
        }

        btnToggleSignUp.setOnClickListener {
            updateToggleUI(isSignIn = false)
        }

        btnMainAction.setOnClickListener {
            if (isSignInMode) {
                handleSignIn()
            } else {
                handleSignUp()
            }
        }

        btnForgot.setOnClickListener {

        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val savedUser = sharedPref.getString("saved_user", "")
        val isRemembered = sharedPref.getBoolean("is_remembered", false)

        if (isRemembered) {
            etSignInUser.setText(savedUser)
            etSignInPass.setText(sharedPref.getString("saved_pass", ""))
            cbRememberMe.isChecked = true

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun handleSignIn() {
        val user = etSignInUser.text.toString().trim()
        val pass = etSignInPass.text.toString()
        val rememberMe = cbRememberMe.isChecked

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Please enter login details", Toast.LENGTH_SHORT).show()
        } else {
            val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
            val editor = sharedPref.edit()

            if (rememberMe) {
                editor.putString("saved_user", user)
                editor.putString("saved_pass", user)
                editor.putBoolean("is_remembered", true)
            } else {
                editor.clear()
            }
            editor.apply()
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

//        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//        val registeredUser = sharedPref.getString("registered_user", "")
//        val registeredPass = sharedPref.getString("registered_pass", "")

//        if (user.isEmpty() || pass.isEmpty()) {
//            Toast.makeText(this, "Please enter login details", Toast.LENGTH_SHORT).show()
//        }
//        else if (user == registeredUser && pass == registeredPass) {
//            val editor = sharedPref.edit()
//
//            if (rememberMe) {
//                editor.putString("saved_user", user)
//                editor.putString("saved_pass", pass)
//                editor.putBoolean("is_remembered", true)
//            } else {
//                editor.remove("saved_user")
//                editor.remove("saved_pass")
//                editor.putBoolean("is_remembered", false)
//            }
//            editor.apply()
//
//            startActivity(Intent(this, HomeActivity::class.java))
//            finish()
//        } else {
//            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
//        }

    private fun handleSignUp() {
        val fName = etFirstName.text.toString().trim()
        val lName = etLastName.text.toString().trim()
        val email = etEmailSignUp.text.toString().trim()
        val uname = etUserSignUp.text.toString()
        val pass = etPassSignUp.text.toString()
        val confirmPass = etPassConfirmSignUp.text.toString()

        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}\$"

        when{
            fName.isEmpty() -> {
                etFirstName.error = "First name required"
                etFirstName.requestFocus()
            }
            lName.isEmpty() -> {
                etLastName.error = "Last name required"
                etLastName.requestFocus()
            }
            email.isEmpty() -> {
                etEmailSignUp.error = "Email required"
                etEmailSignUp.requestFocus()
            }
            uname.isEmpty() -> {
                etUserSignUp.error = "Username required"
                etUserSignUp.requestFocus()
            }
            pass.length < 6 -> {
                etPassSignUp.error = "Password too short"
                etPassSignUp.requestFocus()
            }
            pass != confirmPass -> {
                etPassConfirmSignUp.error = "Passwords do not match"
                etPassConfirmSignUp.requestFocus()
            }

            !email.matches(emailPattern.toRegex()) -> {
                etEmailSignUp.error = "Invalid email address (missing @ or domain)"
                etEmailSignUp.requestFocus()
            }
            !pass.matches(passwordPattern.toRegex()) -> {
                etPassSignUp.error = "Password must contain Upper, Lower, and Number"
                etPassSignUp.requestFocus()
            }

            else -> {
                val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("registered_user", uname)
                editor.putString("registered_pass", pass)
                editor.apply()

                Toast.makeText(this,"Account created for $uname", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
        }
    }

    private fun updateToggleUI(isSignIn: Boolean) {
        this.isSignInMode = isSignIn

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