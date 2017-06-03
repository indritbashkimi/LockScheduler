package com.ibashkimi.lockscheduler.addeditprofile.conditions.picker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Point
import android.location.Criteria
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
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
import com.ibashkimi.lockscheduler.R
import com.ibashkimi.lockscheduler.extention.checkPermission
import com.ibashkimi.lockscheduler.extention.handlePermissionResult
import com.ibashkimi.lockscheduler.extention.requestPermission
import com.ibashkimi.lockscheduler.ui.BaseActivity
import com.ibashkimi.lockscheduler.util.MapUtils
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.util.*

class PlacePickerActivity : BaseActivity(), OnMapReadyCallback {

    internal val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_LOCATION = 99

    lateinit private var mapCoverView: MapCoverView
    lateinit private var addressView: TextView
    lateinit private var radiusView: TextView

    private var googleMap: GoogleMap? = null
    private var mapType = GoogleMap.MAP_TYPE_NORMAL

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
                if (extras.containsKey("map_type")) {
                    mapType = when (extras.getString("map_type")) {
                        "none" -> GoogleMap.MAP_TYPE_NONE
                        "normal" -> GoogleMap.MAP_TYPE_NORMAL
                        "satellite" -> GoogleMap.MAP_TYPE_SATELLITE
                        "hybrid" -> GoogleMap.MAP_TYPE_HYBRID
                        "terrain" -> GoogleMap.MAP_TYPE_TERRAIN
                        else -> throw IllegalArgumentException("Unknown map type ${extras.getString("map_type")}")
                    }
                }
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
            android.R.id.home -> {
                onCancel()
                return true
            }
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

    override fun onPause() {
        super.onPause()
        if (lastJob != null)
            lastJob!!.cancel()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.mapType = this.mapType

        checkPermission(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                whenGranted = { setUpMap(googleMap) },
                whenExplanationNeed = {
                    AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("This app needs the Location permission, please accept to use location functionality")
                            .setPositiveButton(R.string.ok) { _, _ ->
                                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION)
                            }
                            .create().show()
                },
                whenDenied = {
                    requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_REQUEST_LOCATION)
                }
        )
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

    private var lastJob: Job? = null

    private fun updateMap() {
        val projection = googleMap!!.projection
        val cover = mapCoverView
        val startPoint = projection.fromScreenLocation(Point((cover.centerX - cover.radius).toInt(), cover.centerY.toInt()))
        val endPoint = projection.fromScreenLocation(Point(cover.centerX.toInt(), cover.centerY.toInt()))

        // The computed distance is stored in results[0].
        //If results has length 2 or greater, the initial bearing is stored in results[1].
        //If results has length 3 or greater, the final bearing is stored in results[2].
        val results = FloatArray(1)
        Location.distanceBetween(startPoint.latitude, startPoint.longitude,
                endPoint.latitude, endPoint.longitude, results)
        radius = results[0]

        center = projection.visibleRegion.latLngBounds.center
        address = center!!.latitude.toString() + " : " + center!!.longitude.toString()

        addressView.text = address
        radiusView.text = "Radius: ${radius.toInt()} m" // TODO

        updateAddress()
    }

    private fun updateAddress() {
        if (lastJob != null)
            lastJob!!.cancel()
        lastJob = launch(CommonPool) {
            val geocoder = Geocoder(this@PlacePickerActivity, Locale.getDefault())
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            try {
                val addresses = geocoder.getFromLocation(center!!.latitude, center!!.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    if (addresses[0].maxAddressLineIndex > 0) {
                        val lastAddress = addresses[0].getAddressLine(0)!!
                        launch(UI) { setAddress(lastAddress) }
                    }
                }
            } catch (e: IllegalArgumentException) {
                // no op
            } catch (e: IOException) {
                // no op
            }
        }
    }

    private fun setAddress(address: CharSequence) {
        this.address = address
        addressView.text = address
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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_LOCATION -> {
                handlePermissionResult(Manifest.permission.ACCESS_FINE_LOCATION, permissions, grantResults,
                        whenGranted = { setUpMap(googleMap!!) },
                        whenDenied = {
                            Toast.makeText(this, R.string.place_picker_permission_denied, Toast.LENGTH_SHORT).show()
                            setUpBasicMap(googleMap!!)
                        })
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
    }
}
