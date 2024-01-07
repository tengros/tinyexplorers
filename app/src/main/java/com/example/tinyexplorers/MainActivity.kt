package com.example.tinyexplorers

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.api.model.Place

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapView: MapView
    private lateinit var menuClickListener: MenuClickListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth // Lägg till en referens till FirebaseAuth
    private var currentUser: FirebaseUser? = null // Variabel för att lagra aktuell användare
    private lateinit var myLocationButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Places.initialize(applicationContext, "AIzaSyB6jFGRFNx2PK5r8c6sVWj2PyPzlsv-7q8")

        myLocationButton = findViewById(R.id.locationButton)

        // Klicklyssnare för knappen
        myLocationButton.setOnClickListener {
            // Centrera kartan kring användarens nuvarande position
            enableMyLocation()
        }

        // Hämta referenserna till knapparna från layouten
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val accountButton = findViewById<ImageButton>(R.id.accountButton)
        val loginButton = findViewById<ImageButton>(R.id.loginButton)
        val searchButton: ImageButton = findViewById(R.id.searchButton)

        val searchEditText: EditText = findViewById(R.id.searchEditText)



        // Skapa en instans av MenuClickListener och tilldela klicklyssnare till knapparna
        menuClickListener = MenuClickListener(this, findViewById(android.R.id.content))
        menuClickListener.setOnClickListeners(
            settingsButton,
            searchButton,
            accountButton,
            loginButton,
)

        // Hämta aktuell användare från Firebase Authentication
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)




        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Kolla om vi har tillåtelse för platstjänster
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Om inte, be om användarens tillåtelse
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE


            )
        }
        currentUser?.let { user ->
            Log.d("MapReady", "Before fetching user ID")
            val userId = user.uid // Här har du användarens ID
            Log.d("MapReady", "User ID fetched: $userId")
            // Använd userId för att hämta markörer från Firestore
            fetchMarkersFromFirestore(userId)
        }

    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()


        // Lägg till klicklyssnare på kartmarkören
        googleMap.setOnMapLongClickListener { marker ->
            // Kolla om klicket var på en marker
            if (marker != null) {

                // Skapa en AlertDialog för att mata in platsinformation
                val dialogBuilder = AlertDialog.Builder(this)
                val inflater = this.layoutInflater
                val dialogView = inflater.inflate(R.layout.dialog_add_place, null)
                dialogBuilder.setView(dialogView)

                // Hitta referenser till EditText-fälten i dialogrutan
                val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
                val locationEditText = dialogView.findViewById<EditText>(R.id.locationEditText)
                val cityEditText = dialogView.findViewById<EditText>(R.id.cityEditText)
                val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)

                dialogBuilder.setTitle("Lägg till platsinformation")
                dialogBuilder.setPositiveButton("Spara") { _, _ ->
                    // Hämta text från EditText-fälten
                    val name = nameEditText.text.toString()
                    val location = locationEditText.text.toString()
                    val city = cityEditText.text.toString()
                    val description = descriptionEditText.text.toString()

                    // Hämta användarens ID
                    val userId = currentUser?.uid

                    // Skapa ett Place-objekt med inmatad information
                    val place = Place(name, marker.latitude, marker.longitude, location, city, description)

                    // Lägg till platsens information i Firestore under användarens dokument
                    if (userId != null) {
                        savePlaceDetails(userId, place, marker)
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

    private fun savePlaceDetails(userId: String, place: com.example.tinyexplorers.Place, marker: LatLng) {
        val googlePlace = Place.builder()
            .setName(place.name)
            .setLatLng(LatLng(place.latitude, place.longitude))
            // Fyll i resten av fälten baserat på behov
            .build()

        val placeDocument = db.collection("users").document(userId)
            .collection("places")
            .document("${place.latitude}_${place.longitude}")

        placeDocument.set(googlePlace)
            .addOnSuccessListener {
                println("Platsens detaljer sparade i Firestore")
                addMarkerToMap(LatLng(place.latitude, place.longitude), place.name)
            }
            .addOnFailureListener { e ->
                println("Fel vid sparande av platsens detaljer: $e")
            }
    }

    private fun addMarkerToMap(latLng: LatLng, title: String) {
        val markerOptions = MarkerOptions().position(latLng).title(title)
        googleMap.addMarker(markerOptions)
    }

    private val DEFAULT_LOCATION = LatLng( 57.7089, 11.9746) // Stockholm's coordinates as an example

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

                    // Logga när användaren går till sin nuvarande position
                    Log.d("!!!", "User clicked on My Location button.")
                } ?: run {
                    // Handle when location is null or empty
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

            // Om platstillträde inte är beviljat, sätt kartan till standardplatsen
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
                val markersList = ArrayList<Place>()
                for (document in documents) {
                    try {
                        val place = document.toObject(Place::class.java)
                        markersList.add(place)
                    } catch (e: Exception) {
                        Log.e("FetchMarkers", "Error converting document to Place: ${e.message}")
                    }
                }
                // Skapa markörer på kartan baserat på den hämtade platsdatan
                createMarkersOnMap(markersList)
            }
            .addOnFailureListener { e ->
                Log.e("FetchMarkers", "Error fetching documents from Firestore: ${e.message}")
            }
    }

    private fun createMarkersOnMap(markersList: List<Place>) {
        for (place in markersList) {
            val markerLatLng = place.latLng
            markerLatLng?.let {
                val marker = googleMap.addMarker(MarkerOptions().position(it).title(place.name))
                // Anpassa markörens utseende eller lägg till annan information om det behövs
            }
        }
    }

    private fun initPlaceAutoComplete() {
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setPlaceFields(
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // Här kan du få den valda platsens information, som namn och position
                val placeName = place.name
                val placeLatLng = place.latLng

                // Flytta kameran till den valda platsen
                placeLatLng?.let {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }

            override fun onError(status: Status) {
                // Hantera fel här om det behövs
            }
        })
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
