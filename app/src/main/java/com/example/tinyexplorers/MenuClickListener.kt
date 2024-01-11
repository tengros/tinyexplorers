package com.example.tinyexplorers

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.FragmentManager

class MenuClickListener(private val context: Context, private val view: View) {


    fun setOnClickListeners(
        settingsButton: ImageButton,
        searchButton: ImageButton,
        accountButton: ImageButton,
        loginButton: ImageButton,
        supportFragmentManager: FragmentManager
    ) {

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        autocompleteFragment?.view?.visibility = View.GONE

        settingsButton.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
        }

        searchButton.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)

        }

        accountButton.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
        }

        loginButton.setOnClickListener {
            val intent = Intent(context, Login_Activity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
        }
    }
}