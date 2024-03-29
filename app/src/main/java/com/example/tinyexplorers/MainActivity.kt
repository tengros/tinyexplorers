package com.example.tinyexplorers

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.firebase.firestore.FieldValue
import android.content.Context

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var menuClickListener: MenuClickListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth // Lägg till en referens till FirebaseAuth
    private var currentUser: FirebaseUser? = null // Variabel för att lagra aktuell användare
    private lateinit var placeSelectionHelper: PlaceSelectionHelper
    private lateinit var firestore: FirebaseFirestore
    private val temporaryMarkersList = mutableListOf<Marker>()
    private val markersList = mutableListOf<MyPlace>()
    private lateinit var recyclerView: RecyclerView

    private lateinit var placesAdapter: MyPlacesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext, "AIzaSyB6jFGRFNx2PK5r8c6sVWj2PyPzlsv-7q8")

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        placesAdapter = MyPlacesAdapter(this, recyclerView, emptyList()) { place ->
            centerMapOnPlace(place)
        }
        recyclerView.adapter = placesAdapter
        recyclerView.visibility = View.VISIBLE


        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val accountButton = findViewById<ImageButton>(R.id.accountButton)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)
        val searchButton: ImageButton = findViewById(R.id.searchButton)
        searchButton.visibility = View.GONE


        menuClickListener = MenuClickListener(this, findViewById(android.R.id.content))
        menuClickListener.setOnClickListeners(
            settingsButton,
            searchButton,
            accountButton,
            loginButton,
            recyclerView,
            supportFragmentManager
        )
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment)
        autocompleteFragment?.view?.visibility = View.VISIBLE
        settingsButton.visibility = View.VISIBLE


        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        currentUser = auth.currentUser

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE


            )

        }

        currentUser?.let { user ->
            val userId = user.uid
            Log.d("MapReady", "User ID fetched: $userId")
            fetchMarkersFromFirestore(userId)
        }


        placeSelectionHelper = PlaceSelectionHelper(this)
        placeSelectionHelper.initPlaceAutoComplete { selectedPlace ->

            val location = LatLng(selectedPlace.latitude, selectedPlace.longitude)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            addSearchMarker(LatLng(selectedPlace.latitude, selectedPlace.longitude), selectedPlace)
        }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()

        var lastClickTime: Long = 0
        var markerClickedOnce = false

        googleMap.setOnMarkerClickListener { clickedMarker ->
            val currentTime = System.currentTimeMillis()

            if (markerClickedOnce && currentTime - lastClickTime < 500) {
                val userId = currentUser?.uid
                Log.d("userid", "User ID read: $userId")


                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Bekräfta borttagning")
                alertDialogBuilder.setMessage("Är du säker på att du vill ta bort denna markering?")


                alertDialogBuilder.setPositiveButton("Ja") { _, _ ->
                    if (userId != null) {
                        removeMarker(clickedMarker, userId)
                        Log.d("userid", "User ID read: $userId")

                    }
                }


                alertDialogBuilder.setNegativeButton("Avbryt") { dialog, _ ->
                    dialog.dismiss() // Stäng dialogen utan att ta bort markeringen
                }


                alertDialogBuilder.create().show()

                true
            } else {
                markerClickedOnce = true
            }

            lastClickTime = currentTime
            false
        }

        googleMap.setOnMapLongClickListener { marker ->
            if (marker != null) {
                val dialogBuilder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_add_place, null)
                dialogBuilder.setView(dialogView)

                val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
                val locationEditText = dialogView.findViewById<EditText>(R.id.locationEditText)
                val townshipEditText = dialogView.findViewById<EditText>(R.id.townshipEditText)
                val descriptionEditText =
                    dialogView.findViewById<EditText>(R.id.descriptionEditText)

                dialogBuilder.setTitle("Lägg till platsinformation")
                dialogBuilder.setPositiveButton("Spara") { _, _ ->
                    val name = nameEditText.text.toString()
                    val location = locationEditText.text.toString()
                    val township = townshipEditText.text.toString()
                    val description = descriptionEditText.text.toString()

                    val userId = currentUser?.uid
                    userId?.let { fetchMarkersFromFirestore(it) }

                    val place = MyPlace(
                        name,
                        marker.latitude,
                        marker.longitude,
                        location,
                        township,
                        description
                    )

                    if (userId != null) {
                        savePlaceDetails(userId, place)
                    }
                }
                dialogBuilder.setNegativeButton("Avbryt") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = dialogBuilder.create()
                dialog.show()
            }
            true
        }
    }

    private fun removeMarker(marker: Marker, userId: String) {
        db.collection("users").document(userId).collection("places")
            .document("${marker.position.latitude}_${marker.position.longitude}")
            .delete()
            .addOnSuccessListener {
                marker.remove()
                fetchMarkersFromFirestore(userId)
                Log.d("userid", "fetchmarkers $userId")

            }
            .addOnFailureListener { exception ->
                // Hantera fel här
                Log.e("removeMarker", "Fel vid borttagning från Firestore: $exception")
            }

    }

    private fun savePlaceDetails(userId: String, place: MyPlace) {
        val userDocRef = firestore.collection("users").document(userId)

        val placeData = hashMapOf(
            "name" to place.name,
            "latitude" to place.latitude,
            "longitude" to place.longitude,
            "city" to place.city,
            "township" to place.township,
            "description" to place.description

        )

        val placeDocument = db.collection("users").document(userId)
            .collection("places")
            .document("${place.latitude}_${place.longitude}")



        placeDocument.set(placeData)
            .addOnSuccessListener {
                Log.d("Firestore", "Övriga platsdetaljer sparade i Firestore")
                addMarkerToMap(LatLng(place.latitude, place.longitude), place)
            }

            .addOnFailureListener { e ->
                Log.e("Firestore", "Fel vid sparande av övriga platsdetaljer: $e")
            }

    }

    private fun addMarkerToMap(latLng: LatLng, place: MyPlace) {
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(place.name)
            .snippet(place.description)


        googleMap.addMarker(markerOptions)
    }

    private fun addSearchMarker(latLng: LatLng, selectedPlace: MyPlace) {

        clearTemporaryMarkers()

        val temporaryMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(selectedPlace.name)
                .snippet(selectedPlace.city)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
        )

        if (temporaryMarker != null) {
            temporaryMarkersList.add(temporaryMarker)
        }
    }

    private fun clearTemporaryMarkers() {
        for (marker in temporaryMarkersList) {
            marker.remove()
        }
        temporaryMarkersList.clear()
    }

    private val DEFAULT_LOCATION = LatLng(57.7089, 11.9746)

    private fun enableMyLocation() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("!!!", "test")
            googleMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                    Log.d("!!!", "User clicked on My Location button.")
                } ?: run {
                    Log.d("!!!", "Last location is null or empty.")
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12f))
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )


            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 12f))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    private fun fetchMarkersFromFirestore(userId: String) {
        db.collection("users").document(userId).collection("places")
            .get()
            .addOnSuccessListener { documents ->
                val markersList = ArrayList<MyPlace>()
                for (document in documents) {
                    try {
                        val place = document.toObject(MyPlace::class.java)
                        markersList.add(place)
                        addMarkerToMap(LatLng(place.latitude, place.longitude), place)
                    } finally {

                    }
                }

                placesAdapter = MyPlacesAdapter(this, recyclerView, markersList) { selectedPlace ->
                    centerMapOnPlace(selectedPlace)
                }
                recyclerView.adapter = placesAdapter
            }
    }

    private fun centerMapOnPlace(place: MyPlace) {
        val location = LatLng(place.latitude, place.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }


}


// API AIzaSyB6jFGRFNx2PK5r8c6sVWj2PyPzlsv-7q8
