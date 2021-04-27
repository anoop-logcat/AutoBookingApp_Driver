package logcat.ayeautoapps.ayeautodriver2

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapActivity : AppCompatActivity(), PermissionsListener, LocationEngineListener, OnMapReadyCallback {

    private lateinit var permissionManager: PermissionsManager
    private lateinit var map: MapboxMap
    private lateinit var amountText:TextView
    private lateinit var mapbox: MapView

    private val REQUEST_CHECK_SETTINGS = 125
    private val database: DatabaseReference=FirebaseDatabase.getInstance().reference

    private var settingsClient: SettingsClient? = null
    private var destinationLatitude:Double?=null
    private var destinationLongitude:Double?=null
    private var originLocation: Location? = null
    private var locationEngine: LocationEngine? = null
    private var locationComponent: LocationComponent? = null
    private var navigationMapRoute: NavigationMapRoute? = null
    private var currentRoute: DirectionsRoute? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, resources.getString(R.string.MAP_KEY_TOKEN))
        setContentView(R.layout.activity_map)
        val intentData= intent
        mapbox = findViewById(R.id.mapboxMap)
        amountText=findViewById(R.id.navigation_CustomerAmount)
        mapbox.onCreate(savedInstanceState)
        mapbox.getMapAsync(this)
        settingsClient = LocationServices.getSettingsClient(this)
        findViewById<ExtendedFloatingActionButton>(R.id.navigate_fab).text= resourceLanguage?.getString(R.string.start_navigation)
        findViewById<ExtendedFloatingActionButton>(R.id.navigate_fab).setOnClickListener {
            try {
                val navigationLauncherOptions = NavigationLauncherOptions.builder()
                        .directionsRoute(currentRoute)
                        .shouldSimulateRoute(false)
                        .build()
                NavigationLauncher.startNavigation(this, navigationLauncherOptions)
            }catch (e:Exception){
                Toast.makeText(this, resourceLanguage!!.getString(R.string.waiting_for_route), Toast.LENGTH_LONG).show()
            }
        }
        findViewById<ExtendedFloatingActionButton>(R.id.call_customer).text= resourceLanguage?.getString(R.string.call_now)
        findViewById<ExtendedFloatingActionButton>(R.id.call_customer).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 42)
                }
                else{
                    Toast.makeText(this, resourceLanguage?.getString(R.string.Permission_denied),Toast.LENGTH_SHORT).show()
                }
            } else {
                startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:${intentData.getStringExtra("phone")}")))
            }
        }
        amountText.text= "${resourceLanguage?.getString(R.string.travel_rate)} ${intentData.getStringExtra("amount")} ${resourceLanguage?.getString(R.string.rupees)}"
        destinationLatitude = (intentData.getStringExtra("latitude"))?.toDouble()
        destinationLongitude= (intentData.getStringExtra("longitude"))?.toDouble()
        findViewById<TextView>(R.id.navigation_CustomerName).text=  intentData.getStringExtra("name")
        findViewById<TextView>(R.id.navigation_CustomerLocationFrom).text= intentData.getStringExtra("location").toString().split("_").first()
        findViewById<TextView>(R.id.navigation_CustomerLocationTo).text= intentData.getStringExtra("location").toString().split("_").last()
    }

    private fun getRoute(originPoint: Point, endPoint: Point) {
        NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(originPoint)
            .destination(endPoint)
            .build()
            .getRoute(object : Callback<DirectionsResponse> {
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {

                }

                override fun onResponse(
                    call: Call<DirectionsResponse>,
                    response: Response<DirectionsResponse>
                ) {
                    if (navigationMapRoute != null) {
                        navigationMapRoute?.updateRouteVisibilityTo(false)
                    } else {
                        navigationMapRoute = NavigationMapRoute(null, mapbox, map)
                    }
                    currentRoute = response.body()?.routes()?.first()
                    if (currentRoute != null) {
                        navigationMapRoute?.addRoute(currentRoute)
                    }
                }
            })
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            initializeLocationComponent()
            initializeLocationEngine()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this)
        }
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationComponent() {
        locationComponent = map.locationComponent
        locationComponent?.activateLocationComponent(this)
        locationComponent?.isLocationComponentEnabled = true
        locationComponent?.cameraMode = CameraMode.TRACKING
    }

    @SuppressWarnings("MissingPermission")
    private fun initializeLocationEngine() {
        locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine?.activate()
        locationEngine?.addLocationEngineListener(this)
        val lastLocation = locationEngine?.lastLocation
        if (lastLocation != null) {
            originLocation = lastLocation
            setCameraPosition(lastLocation)
        } else {
            locationEngine?.addLocationEngineListener(this)
        }
    }

    private fun setDestinationLocation(){
        originLocation?.run {
            val startPoint = Point.fromLngLat(longitude, latitude)
            val endPoint = Point.fromLngLat(destinationLongitude!!, destinationLatitude!!)
            val destinationMarker = map.addMarker(MarkerOptions().position(LatLng(destinationLatitude!!,destinationLongitude!!)))
            destinationMarker.icon= IconFactory.recreate(resources.getString(R.string.DESTINATION_ICON_ID), BitmapFactory.decodeResource(resources, R.drawable.customer_position_icon))
            getRoute(startPoint, endPoint)
        }
    }

    private fun setCameraPosition(location: Location) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), 15.0
            )
        )
    }

    @SuppressWarnings("MissingPermission")
    override fun onStart() {
        super.onStart()
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationEngine?.requestLocationUpdates()
            locationComponent?.onStart()
        }
        mapbox.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapbox.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapbox.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine?.removeLocationUpdates()
        locationComponent?.onStop()
        mapbox.onStop()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resourceLanguage!!.getString(R.string.warning))
        builder.setMessage(resourceLanguage!!.getString(R.string.warning_sub_text))
        builder.setPositiveButton(resourceLanguage!!.getString(R.string.accept)) { _, _ ->
            locationDatabase(false,originLocation!!.latitude.toString(),originLocation!!.longitude.toString())
            finish()
        }
        builder.setNegativeButton(resourceLanguage!!.getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine?.deactivate()
        mapbox.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapbox.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        mapbox.onSaveInstanceState(outState)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "This app needs location permission to be able to show your location on the map",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        } else {
            Toast.makeText(this, "User location was not granted", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "permission not acquired", Toast.LENGTH_LONG).show()
        }
        else{
            locationEngine?.requestLocationUpdates()
        }
    }

    override fun onLocationChanged(location: Location?) {
        location?.run {
            originLocation = this
            setCameraPosition(this)
            locationDatabase(true,this.latitude.toString(),this.longitude.toString())
            setDestinationLocation()
        }
    }

    private fun locationDatabase(sharing: Boolean,lat: String,long: String){
        val locationObject = LocationClass(sharing,lat,long)
        database.child("DriversNavigation").child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(locationObject).addOnCompleteListener {
            if(!it.isSuccessful){
                Toast.makeText(baseContext,"Failed to upload location",Toast.LENGTH_SHORT).show()
            }
        }
    }

    data class LocationClass(val sharing:Boolean,val lat:String,val long:String)

    override fun onMapReady(mapboxMap: MapboxMap?) {
        map = mapboxMap ?: return
        val locationRequestBuilder = LocationSettingsRequest.Builder().addLocationRequest(
            LocationRequest().setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY
            )
        )
        val locationRequest = locationRequestBuilder?.build()
        settingsClient?.checkLocationSettings(locationRequest)?.run {
            addOnSuccessListener {
                enableLocation()
            }
            addOnFailureListener {
                val statusCode = (it as ApiException).statusCode
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    val resolvableException = it as? ResolvableApiException
                    resolvableException?.startResolutionForResult(
                        this@MapActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                enableLocation()
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }
    }
}