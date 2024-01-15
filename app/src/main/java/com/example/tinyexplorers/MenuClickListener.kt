package com.example.tinyexplorers

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView

class MenuClickListener(private val context: Context, private val view: View) {
    private var isRecyclerViewExpanded = false

    fun setOnClickListeners(
        settingsButton: ImageButton,
        searchButton: ImageButton,
        accountButton: ImageButton,
        loginButton: ImageButton,
        recyclerView: RecyclerView,
        supportFragmentManager: FragmentManager
    ) {

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        autocompleteFragment?.view?.visibility = View.GONE




        settingsButton.setOnClickListener {
            if (isRecyclerViewExpanded) {
                // Återgå till ursprungsläge
                val originalHeightInDp = 84
                val scale = context.resources.displayMetrics.density
                val originalHeightInPixels = (originalHeightInDp * scale + 0.5f).toInt()

                val layoutParams = recyclerView.layoutParams
                layoutParams.height = originalHeightInPixels
                recyclerView.layoutParams = layoutParams

                isRecyclerViewExpanded = false
            } else {
                // Förstora upp RecyclerView
                val newHeightInDp = 671
                val scale = context.resources.displayMetrics.density
                val newHeightInPixels = (newHeightInDp * scale + 0.5f).toInt()

                val layoutParams = recyclerView.layoutParams
                layoutParams.height = newHeightInPixels
                recyclerView.layoutParams = layoutParams

                isRecyclerViewExpanded = true
            }

            // Visa/göm autocompleteFragment beroende på om RecyclerView är utökad eller inte
            autocompleteFragment?.view?.visibility = if (isRecyclerViewExpanded) View.GONE else View.VISIBLE
            searchButton.visibility = if (isRecyclerViewExpanded) View.VISIBLE else View.GONE

        }


       searchButton.setOnClickListener {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
            recyclerView.visibility = View.GONE


        }

        accountButton.setOnClickListener {
            val intent = Intent(context, AccountActivity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }

        loginButton.setOnClickListener {
            val intent = Intent(context, Login_Activity::class.java)
            context.startActivity(intent)
            autocompleteFragment?.view?.visibility = View.GONE
            recyclerView.visibility = View.GONE

        }
    }}
