package com.example.recipegenerator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.example.recipegenerator.ui.viewmodel.AuthViewModel
import com.example.recipegenerator.ui.viewmodel.AuthViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var btnToggleSignIn: Button
    private lateinit var btnToggleSignUp: Button
    private lateinit var btnMainAction: Button
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvSubTitle: TextView
    private lateinit var layoutSignInFields: LinearLayout
    private lateinit var layoutSignUpFields: LinearLayout
    private lateinit var etSignInEmail: TextInputEditText
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
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val sharedPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        val currentUser = auth.currentUser
        val isRemembered = sharedPrefs.getBoolean("is_remembered", false)

        if (currentUser != null && currentUser.isEmailVerified && isRemembered) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        if (currentUser != null && !isRemembered) {
            auth.signOut()
        }

        googleSignInLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val user = authTask.result?.user
                            user?.let { authViewModel.handleGoogleSignIn(it) }
                        } else {
                            Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        authViewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(
                userDao = (application as RecipeApp).userDao,
                sharedPrefs = sharedPrefs
            )
        )[AuthViewModel::class.java]

        authViewModel.authResult.observe(this) { result ->
            when (result) {
                is AuthViewModel.AuthResult.Success -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                is AuthViewModel.AuthResult.NeedsVerification -> {
                    Toast.makeText(this, "Verification email sent! Please check your inbox.", Toast.LENGTH_LONG).show()
                    updateToggleUI(isSignIn = true)
                }
                is AuthViewModel.AuthResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                    if (result.message.contains("already registered", ignoreCase = true)) {
                        updateToggleUI(isSignIn = true)
                    }
                }
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
        etSignInEmail = findViewById(R.id.etSignInEmail)
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

        updateToggleUI(isSignIn = true)

        btnToggleSignIn.setOnClickListener { updateToggleUI(isSignIn = true) }
        btnToggleSignUp.setOnClickListener { updateToggleUI(isSignIn = false) }

        btnMainAction.setOnClickListener {
            if (isSignInMode) handleSignIn() else handleSignUp()
        }

        btnForgot.setOnClickListener {
            val email = etSignInEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                authViewModel.sendPasswordResetEmail(email)
                Toast.makeText(this, "Checking for account: $email", Toast.LENGTH_SHORT).show()
            } else {
                etSignInEmail.error = "Enter your email here first"
                etSignInEmail.requestFocus()
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }

        btnSignInGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleSignIn() {
        val email = etSignInEmail.text.toString().trim()
        val pass = etSignInPass.text.toString()
        val rememberMe = cbRememberMe.isChecked

        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"

        when {
            email.isEmpty() -> {
                etSignInEmail.error = "Email required"
                etSignInEmail.requestFocus()
            }
            !email.matches(emailPattern.toRegex()) -> {
                etSignInEmail.error = "Please enter a valid email"
                etSignInEmail.requestFocus()
            }
            pass.isEmpty() -> {
                etSignInPass.error = "Password Required"
                etSignInPass.requestFocus()
            }
            else -> {
                authViewModel.signIn(email, pass, rememberMe)
            }
        }
    }

    private fun handleSignUp() {
        val fName = etFirstName.text.toString().trim()
        val lName = etLastName.text.toString().trim()
        val email = etEmailSignUp.text.toString().trim()
        val uname = etUserSignUp.text.toString().trim()
        val pass = etPassSignUp.text.toString()
        val confirmPass = etPassConfirmSignUp.text.toString()

        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}\$"

        when {
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
                // Save to Room via AuthViewModel
                authViewModel.signUp(fName, lName, uname, email, pass)
            }
        }
    }

    private fun updateToggleUI(isSignIn: Boolean) {
        this.isSignInMode = isSignIn

        if (isSignIn) {
            btnToggleSignIn.setBackgroundResource(R.drawable.bg_tab_container)
            btnToggleSignIn.setTextColor(Color.WHITE)
            btnToggleSignUp.background = null
            btnToggleSignUp.setTextColor(Color.BLACK)
            tvTitle.text = getString(R.string.signin_title)
            tvSubTitle.text = "Ready to cook something good? Let's check your ingredients and find out what you can eat."
            btnMainAction.text = "Sign In"
            layoutSignInFields.visibility = View.VISIBLE
            layoutSignUpFields.visibility = View.GONE
        } else {
            btnToggleSignUp.setBackgroundResource(R.drawable.bg_tab_container)
            btnToggleSignUp.setTextColor(Color.WHITE)
            btnToggleSignIn.background = null
            btnToggleSignIn.setTextColor(Color.BLACK)
            tvTitle.text = getString(R.string.signup_title)
            tvSubTitle.text = "Wanna start cooking smarter? Sign up to track ingredients and find safe recipes."
            btnMainAction.text = "Sign Up"
            layoutSignInFields.visibility = View.GONE
            layoutSignUpFields.visibility = View.VISIBLE
        }
    }
}