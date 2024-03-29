package com.example.tinyexplorers

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class Login_Activity : AppCompatActivity() {


    private lateinit var menuClickListener: MenuClickListener
    private val recyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerViewMain) ?: RecyclerView(this)
    }

    lateinit var auth: FirebaseAuth
    lateinit var firestore: FirebaseFirestore
    lateinit var emailView: EditText
    lateinit var passwordView: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = View.GONE

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        settingsButton.isEnabled = false
        settingsButton.isClickable = false
        settingsButton.alpha = 0.5f
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        val accountButton = findViewById<ImageButton>(R.id.accountButton)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)
        searchButton.visibility = View.VISIBLE


        menuClickListener = MenuClickListener(this, findViewById(android.R.id.content))
        menuClickListener.setOnClickListeners(
            settingsButton,
            searchButton,
            accountButton,
            loginButton,
            recyclerView,
            supportFragmentManager
        )


        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

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
            Toast.makeText(
                this@Login_Activity,
                "Vänligen skriv in både användarnamn och lösenord",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    goToAddActivity()
                } else {
                    Toast.makeText(
                        this@Login_Activity,
                        "Felaktigt användarnamn eller lösenord",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }


    }

    fun goToAddActivity() {
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)

    }

    @SuppressLint("SimpleDateFormat")
    fun signUp() {
        val email = emailView.text.toString()
        val password = passwordView.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val userDocRef = firestore.collection("users").document(userId)

                        val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                        val initialData = hashMapOf(
                            "memberSinceDate" to currentDate,
                            "savedPlacesCount" to 0
                        )

                        userDocRef.set(initialData)
                            .addOnSuccessListener {
                                goToAddActivity()
                            }
                            .addOnFailureListener { exception ->
                                signIn()
                            }
                    }
                } else {
                    signIn()
                }
            }
    }
}
