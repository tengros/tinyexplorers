package com.example.tinyexplorers

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

data class MyPlace(
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: String = "",
    val township: String = "",
    val description: String = ""
    // Lägg till andra attribut om nödvändigt
)

class PlaceSelectionHelper(private val context: MainActivity) {
    fun initPlaceAutoComplete(onPlaceSelected: (MyPlace) -> Unit) {
        val autocompleteFragment =
            (context as FragmentActivity).supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val myPlace = convertToMyPlace(place)
                onPlaceSelected.invoke(myPlace)
            }

            override fun onError(status: Status) {
                // Hantera fel här om det behövs
            }
        })
    }



    private fun convertToMyPlace(place: Place): MyPlace {
        return MyPlace(
            name = place.name.toString(),
            latitude = place.latLng?.latitude ?: 0.0,
            longitude = place.latLng?.longitude ?: 0.0,
            city = place.address.toString(),
            description = place.address?.toString() ?: ""
            // Lägg till andra attribut om nödvändigt
        )
    }


}
