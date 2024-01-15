package com.example.tinyexplorers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.content.Context

class MyPlacesAdapter(private val context: Context, private val places: List<MyPlace>) : RecyclerView.Adapter<MyPlacesAdapter.PlaceViewHolder>()  {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.placeNameTextView)
        val placeDescriptionTextView: TextView = itemView.findViewById(R.id.placeDescriptionTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.placeNameTextView.text = place.name
        holder.placeDescriptionTextView.text = place.description

        holder.itemView.setOnClickListener {
            showPlaceDialog(place)
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }

    private fun showPlaceDialog(place: MyPlace) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(place.name)
        alertDialogBuilder.setMessage("Beskrivning: ${place.description}\nStad: ${place.city}\nPlats: ${place.township}")

        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }

        alertDialogBuilder.create().show()
    }
}