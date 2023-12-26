package com.example.tinyexplorers

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton

class MenuClickListener(private val context: Context, private val view: View) {

    fun setOnClickListeners(settingsButton: ImageButton, searchButton: ImageButton,
                            accountButton: ImageButton, loginButton: ImageButton, ) {
        var isSearchVisible = false

        settingsButton.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            context.startActivity(intent)
        }

        searchButton.setOnClickListener {
            // Hitta referensen till EditText-elementet
            val searchEditText = view.findViewById<EditText>(R.id.searchEditText)

            // Om sökrutan är synlig ska den döljas när användaren klickar på sökknappen
            if (isSearchVisible) {
                // Dölj sökrutan
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                searchEditText.visibility = View.GONE
                isSearchVisible = false

            } else {
                // Visa sökrutan
                searchEditText.visibility = View.VISIBLE
                isSearchVisible = true
            }
        }

        accountButton.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            context.startActivity(intent)
        }

        loginButton.setOnClickListener {
            val intent = Intent(context, Login_Activity::class.java)
            context.startActivity(intent)
        }
    }
}