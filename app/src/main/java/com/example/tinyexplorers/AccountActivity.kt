package com.example.tinyexplorers

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {
    private lateinit var menuClickListener: MenuClickListener
    private lateinit var userTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val recyclerView by lazy { findViewById<RecyclerView>(R.id.recyclerViewMain) ?: RecyclerView(this) }


    var savedPlacesCount: Int = 0
    private var memberSinceDate: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.visibility = View.GONE


        val savedPlacesTextView = findViewById<TextView>(R.id.savedPlacesTextView)
        val memberSinceTextView = findViewById<TextView>(R.id.memberSinceTextView)

        // Hämta referenserna till knapparna från layouten
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val searchButton = findViewById<ImageButton>(R.id.searchButton)
        val accountButton = findViewById<ImageButton>(R.id.accountButton)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)
        searchButton.visibility = View.VISIBLE

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        autocompleteFragment?.view?.visibility = View.GONE



        // Skapa en instans av MenuClickListener och tilldela klicklyssnare till knapparna
        menuClickListener = MenuClickListener(this, findViewById(android.R.id.content))
        menuClickListener.setOnClickListeners(
            settingsButton,
            searchButton,
            accountButton,
            loginButton,
            recyclerView,
            supportFragmentManager
        )


        // Initialisera Firebase Auth och Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Hämta referens till TextView
        userTextView = findViewById(R.id.userTextView)



        // Hämta referens till logoutButton
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            // Efter utloggningen, navigera tillbaka till login- eller registreringsskärmen
            val intent = Intent(this, Login_Activity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userEmail = currentUser.email
                        userTextView.text = "Du är inloggad som: $userEmail"

                        // Hämta värden från dokumentet i Firestore och uppdatera variablerna
                        savedPlacesCount = document.getLong("savedPlacesCount")?.toInt() ?: 0
                        memberSinceDate = document.getString("memberSinceDate") ?: ""

                        // Uppdatera texten i dina TextView-element
                        savedPlacesTextView.text = "Antal sparade platser: $savedPlacesCount"
                        memberSinceTextView.text = "Medlem sedan: $memberSinceDate"
                    } else {
                        userTextView.text = "Ingen användare är för närvarande inloggad"
                    }
                }
            }.addOnFailureListener { exception ->
                userTextView.text = "Fel vid hämtning av användardata: $exception"
            }
        } else {
            userTextView.text = "Ingen användare är för närvarande inloggad"

        }
    }}
