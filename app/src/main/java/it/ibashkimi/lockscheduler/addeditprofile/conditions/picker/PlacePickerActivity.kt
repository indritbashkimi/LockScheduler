package it.ibashkimi.lockscheduler.addeditprofile.conditions.picker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import it.ibashkimi.lockscheduler.R
import it.ibashkimi.lockscheduler.ui.BaseActivity
import it.ibashkimi.lockscheduler.util.MapUtils
import java.util.*

class PlacePickerActivity : BaseActivity(), OnMapReadyCallback {

    internal var PLACE_AUTOCOMPLETE_REQUEST_CODE = 1

    private var mapCoverView: MapCoverView? = null
    private var addressView: TextView? = null
    private var radiusView: TextView? = null

    private var googleMap: GoogleMap? = null

    private var center: LatLng? = null
    private var address: CharSequence? = null
    private var radius: Float = 300f
    private var margin: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_picker)

        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setHomeButtonEnabled(true)
            actionBar.setDefaultDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        margin = resources.getDimensionPixelOffset(R.dimen.place_picker_circle_margin)

        if (savedInstanceState == null) {
            val extras = intent.extras
            if (extras != null) {
                if (extras.containsKey("latitude"))
                    center = LatLng(extras.getDouble("latitude"), extras.getDouble("longitude"))
                if (extras.containsKey("radius"))
                    radius = extras.getInt("radius").toFloat()
            }
        } else {
            center = savedInstanceState.getParcelable<LatLng>("center")
            radius = savedInstanceState.getFloat("radius")
        }

        addressView = findViewById(R.id.address) as TextView
        radiusView = findViewById(R.id.radius) as TextView
        mapCoverView = findViewById(R.id.mapCover) as MapCoverView
        findViewById(R.id.selectLocationCard).setOnClickListener({ onSave() })

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as MapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putParcelable("center", center)
        outState?.putFloat("radius", radius)
    }

    override fun onResume() {
        super.onResume()
        if (googleMap != null && center != null) {
            val bounds = MapUtils.calculateBounds(center, radius.toDouble())
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin)
            // Show the current location in Google Map
            googleMap!!.moveCamera(cameraUpdate)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.place_picker, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onCancel()
            R.id.action_place_search -> {
                try {
                    val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: GooglePlayServicesRepairableException) {
                    e.printStackTrace()
                    throw e
                } catch (e: GooglePlayServicesNotAvailableException) {
                    e.printStackTrace()
                    throw e
                }

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                setUpMap(googleMap)
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            setUpMap(googleMap)
        }
    }

    private fun setUpMap(googleMap: GoogleMap) {
        googleMap.isMyLocationEnabled = true

        if (center == null) {
            // Get LocationManager object from System Service LOCATION_SERVICE
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            // Create a criteria object to retrieve provider
            val criteria = Criteria()

            // Get the name of the best provider
            val provider = locationManager.getBestProvider(criteria, true)

            // Get Current Location
            val myLocation = locationManager.getLastKnownLocation(provider)
            center = LatLng(myLocation.latitude, myLocation.longitude)
            address = center!!.latitude.toString() + ": " + center!!.longitude.toString()
        }

        val bounds = MapUtils.calculateBounds(center, radius.toDouble())
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin)
        // Show the current location in Google Map
        googleMap.moveCamera(cameraUpdate)

        googleMap.setOnCameraIdleListener({
            updateMap()
        })
    }

    private fun setUpBasicMap(googleMap: GoogleMap) {
        updateMap()
        googleMap.setOnCameraIdleListener({
            updateMap()
        })
    }

    private fun updateMap() {
        val projection = googleMap!!.projection
        val cover = mapCoverView!!
        val startPoint = projection.fromScreenLocation(Point((cover.centerX - cover.radius).toInt(), cover.centerY.toInt()))
        val endPoint = projection.fromScreenLocation(Point(cover.centerX.toInt(), cover.centerY.toInt()))

        // The computed distance is stored in results[0].
        //If results has length 2 or greater, the initial bearing is stored in results[1].
        //If results has length 3 or greater, the final bearing is stored in results[2].
        val results = FloatArray(1)
        Location.distanceBetween(startPoint.latitude, startPoint.longitude,
                endPoint.latitude, endPoint.longitude, results)
        radius = results[0]

        val geocoder = Geocoder(this, Locale.getDefault())
        center = projection.visibleRegion.latLngBounds.center
        address = center!!.latitude.toString() + " : " + center!!.longitude.toString()

        addressView?.text = address
        radiusView?.text = "Radius: ${radius.toInt()} m" // TODO

        // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val addresses = geocoder.getFromLocation(center!!.latitude, center!!.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            if (addresses[0].maxAddressLineIndex > 0) {
                address = addresses[0].getAddressLine(0)
                addressView?.text = address
            }
        }
    }

    fun onCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    fun onSave() {
        if (center != null) {
            val resultIntent = Intent()
            resultIntent.putExtra("latitude", center!!.latitude)
            resultIntent.putExtra("longitude", center!!.longitude)
            resultIntent.putExtra("radius", radius)
            resultIntent.putExtra("address", address)
            setResult(Activity.RESULT_OK, resultIntent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK") { _, _ ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@PlacePickerActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        }
                        .create()
                        .show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        setUpMap(googleMap!!)
                    }
                } else {
                    Toast.makeText(this, R.string.place_picker_permission_denied, Toast.LENGTH_SHORT).show()
                    setUpBasicMap(googleMap!!)
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                this.center = place.latLng
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                val status = PlaceAutocomplete.getStatus(this, data)
                Log.i(TAG, status.statusMessage)
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    companion object {

        private val TAG = PlacePickerActivity::class.java.simpleName

        val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
