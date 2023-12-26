package com.example.tinyexplorers

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class Login_Activity : AppCompatActivity() {



    private lateinit var menuClickListener: MenuClickListener
    lateinit var auth: FirebaseAuth
    lateinit var emailView: EditText
    lateinit var passwordView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hämta referenserna till knapparna från layouten
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        val accountButton = findViewById<ImageButton>(R.id.accountButton)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)

        // Skapa en instans av MenuClickListener och tilldela klicklyssnare till knapparna
        menuClickListener = MenuClickListener(this, findViewById(android.R.id.content))
        menuClickListener.setOnClickListeners(
            settingsButton,
            searchButton,
            accountButton,
            loginButton
        )

        auth = Firebase.auth
        emailView = findViewById(R.id.EmailEditTextText)
        passwordView = findViewById(R.id.PasswordEditTextText)

        val signUpButton = findViewById<Button>(R.id.SignUpButton)
        signUpButton.setOnClickListener {
            signUp()
        }

        val signInButton = findViewById<Button>(R.id.SignInButton)
        signInButton.setOnClickListener {
            signIn()
        }

        if (auth.currentUser != null) {
            goToAddActivity()
        }
    }

    fun signIn() {
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this@Login_Activity, "Vänligen skriv in både användarnamn och lösenord", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("!!!", "signed in")
                    goToAddActivity()
                } else {
                    Log.d("!!!", "user not signed in ${task.exception}")
                    // Visa ett meddelande med Toast
                    Toast.makeText(this@Login_Activity, "Felaktigt användarnamn eller lösenord", Toast.LENGTH_SHORT).show()
                }

            }


    }

    fun goToAddActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)

    }

    fun signUp() {
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("!!!", "create sucess")
                    goToAddActivity()
                } else {
                    Log.d("!!!", "user not created ${task.exception}")
                }

            }
    }
}