package com.example.tinyexplorers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyPlacesAdapter(private val places: List<MyPlace>) : RecyclerView.Adapter<MyPlacesAdapter.PlaceViewHolder>() {

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
    }

    override fun getItemCount(): Int {
        return places.size
    }
}