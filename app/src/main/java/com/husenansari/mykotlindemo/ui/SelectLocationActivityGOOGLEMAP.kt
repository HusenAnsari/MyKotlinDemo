package com.husenansari.mykotlindemo.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.gson.Gson
import com.hashtechhub.mrhomeexpert.R
import com.hashtechhub.mrhomeexpert.api.ApiInterface
import com.hashtechhub.mrhomeexpert.api.model.*
import com.hashtechhub.mrhomeexpert.helper.AppConstant
import com.hashtechhub.mrhomeexpert.helper.MyApplication
import com.hashtechhub.mrhomeexpert.helper.SharedPrefManager
import com.timechart.tctracker.helper.Functions
import kotlinx.android.synthetic.main.activity_select_location.*
import kotlinx.android.synthetic.main.toolbar.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*


class SelectLocationActivityGOOGLEMAP : BaseActivity(), OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {

    private var map: SupportMapFragment? = null
    private var mMap: GoogleMap? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private var isFirstTime: Boolean? = false
    var geocoder: Geocoder? = null
    var addressFinal: String? = null
    var onMapTapLong: String? = null
    var onMapTapLat: String? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private var screen_type: String? = null
    private var radioButton: RadioButton? = null
    private var address_value: String? = null
    private var other_type: String? = null
    private var city_name: String? = null
    private var address: Addersse? = null
    var loader: ProgressDialog? = null
    private var finalLatitude: String? = null
    private var finalLongitude: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        screen_type = intent.getStringExtra(AppConstant.TYPE)


        initToolbar()
        initView()

    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        loader = Functions.getLoader(this)


        when (screen_type) {
            AppConstant.IS_ADD_ADDRESS -> {
                llAddressSection.visibility = View.VISIBLE
            }
            AppConstant.IS_EDIT_ADDRESS -> {
                llAddressSection.visibility = View.VISIBLE
                address = Gson().fromJson(
                    intent.getStringExtra(AppConstant.ADDRESS),
                    Addersse::class.java
                )
                setAddressValue(address)
            }
            else -> {
                llAddressSection.visibility = View.GONE
            }
        }



        rgAddressType.setOnCheckedChangeListener { group, checkedId ->
            val rb = findViewById<View>(checkedId) as RadioButton

            when {
                rb.text.toString() == resources.getString(R.string.others) -> {
                    edtLabel.visibility = View.VISIBLE
                }
                else -> {
                    edtLabel.visibility = View.GONE
                }
            }
        }

        map = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        map!!.getMapAsync(this)



        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, getString(R.string.api_key), Locale.US);
        }


        txtDetectMyLocation.setOnClickListener {
            val lat = SharedPrefManager.getLatitude(this)!!
            val long = SharedPrefManager.getLongitude(this)!!

            finalLatitude = lat
            finalLongitude = long

            val center: CameraUpdate = CameraUpdateFactory.newLatLng(
                try {
                    LatLng(
                        lat.toDouble(),
                        long.toDouble()
                    )
                } catch (e: Exception) {
                } as LatLng?
            )

            Log.e("latlong", "$lat, $long")
            val zoom: CameraUpdate = CameraUpdateFactory.zoomTo(18f)

            mMap!!.moveCamera(center)
            mMap!!.animateCamera(zoom)


            val address = getAddress(lat.toDouble(), long.toDouble())
            addressFinal = address
            edtAddress.setText(addressFinal)


        }

        llTapToSelect.setOnClickListener {
            //Toast.makeText(this!!, getString(R.string.under_dev), Toast.LENGTH_SHORT).show()
            if (onMapTapLat !== null && onMapTapLong != null) {
                val address = getAddress(onMapTapLat!!.toDouble(), onMapTapLong!!.toDouble())
                addressFinal = address
                finalLatitude = onMapTapLat
                finalLongitude = onMapTapLong
                edtAddress.setText(addressFinal)
            } else {
                Toast.makeText(this!!, " Tap on map to select another address", Toast.LENGTH_SHORT)
                    .show()
            }
        }



        btnConfirmLocation.setOnClickListener {
            if (screen_type == AppConstant.IS_SELECT_LOCATION) {
                if (addressFinal != null) {
                    SharedPrefManager.setMapAddress(this, addressFinal!!)
                    if (Functions.isConnected(this)) {
                        callSetLocation()
                    } else {
                        Functions.showToast(this, getString(R.string.internet_error))
                    }
                } else {
                    Functions.showToast(this, "Please select address")
                    return@setOnClickListener
                }

            } else {
                checkValidation()
            }
        }

        edtAddress.setOnClickListener {
            val fields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            val intent =
                Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(
                    this
                )
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }


    }

    private fun callSetLocation() {
        /*  Log.e("api-lat", finalLatitude.toString())
          Log.e("api-long", finalLongitude.toString())
          Log.e("api-city", city_name.toString())
          Log.e("api-address", addressFinal.toString())
          finish()*/


        loader!!.show()

        val setLocationRequest = SetLocationRequest()
        setLocationRequest.user_id = SharedPrefManager.getUserID(this).toString()
        setLocationRequest.address = addressFinal.toString()
        setLocationRequest.city = city_name.toString()
        setLocationRequest.lat = finalLatitude.toString()
        setLocationRequest.lang = finalLongitude.toString()


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.setlocation(setLocationRequest).enqueue(object : Callback<OrderCancelledResponse> {
            override fun onResponse(
                call: Call<OrderCancelledResponse>,
                response: Response<OrderCancelledResponse>
            ) {
                loader!!.dismiss()
                val orderCancelledResponse: OrderCancelledResponse = response.body()!!

                if (orderCancelledResponse.status_code == 200) {
                    finish()
                } else {
                    Functions.showToast(this@SelectLocationActivityGOOGLEMAP, orderCancelledResponse.message)
                }
            }

            override fun onFailure(call: Call<OrderCancelledResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(
                    this@SelectLocationActivityGOOGLEMAP,
                    getString(R.string.someting_want_wrong)
                )
            }
        })
    }

    private fun setAddressValue(address: Addersse?) {

        edtAddressName.setText(address!!.user_name)
        edtHouseNo.setText(address!!.address)
        edtLandMark.setText(address!!.landmark)

        when {
            address.address_type.equals("Home", ignoreCase = true) -> {
                rbHome.isChecked = true
            }
            address.address_type.equals("Work", ignoreCase = true) -> {
                rbWork.isChecked = true
            }
            address.address_type.equals("Other", ignoreCase = true) -> {
                rbOther.isChecked = true
                edtLabel.visibility = View.VISIBLE
                edtLabel.setText(address.other_type)
            }
        }
    }

    private fun checkValidation() {
        if (edtAddressName.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter name")
            return
        }

        if (edtHouseNo.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter address")
            return
        }

        if (edtLandMark.text!!.isEmpty()) {
            Functions.showToast(this, "Please enter landmark")
            return
        }

        if (rgAddressType.checkedRadioButtonId == -1) {
            Functions.showToast(this, "Select Address Type")
            return
        }

        val selectedId: Int = rgAddressType.checkedRadioButtonId
        radioButton = findViewById<RadioButton>(selectedId)

        if (radioButton!!.text == getString(R.string.others)) {
            if (edtLabel.text!!.isEmpty()) {
                Functions.showToast(this, "Please enter label")
                return
            }

        }

        if (radioButton!!.text == getString(R.string.home)) {
            address_value = "Home"
            other_type = ""
        } else if (radioButton!!.text == getString(R.string.work)) {
            address_value = "Work"
            other_type = ""
        } else if (radioButton!!.text == getString(R.string.others)) {
            address_value = "Other"
            other_type = edtLabel.text!!.toString()
        }


        if (Functions.isConnected(this)) {
            if (screen_type == AppConstant.IS_ADD_ADDRESS) {
                callAddAddress()
            } else if (screen_type == AppConstant.IS_EDIT_ADDRESS) {
                callEditAddress()
            }
        } else {
            Functions.showToast(this, getString(R.string.internet_error))
        }


    }

    private fun callEditAddress() {
        loader!!.show()

        val editAddressRequest = EditAddressRequest()
        editAddressRequest.user_id = SharedPrefManager.getUserID(this).toString()
        editAddressRequest.address_id = address!!.address_id
        editAddressRequest.address_type = address_value
        editAddressRequest.phone = SharedPrefManager.getUserData(this)!!.phone
        editAddressRequest.name = edtAddressName.text.toString()
        editAddressRequest.address = edtHouseNo.text!!.toString()
        editAddressRequest.landmark = edtLandMark.text!!.toString()
        editAddressRequest.other_type = other_type


        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.edit_address(editAddressRequest).enqueue(object : Callback<AddEditAddressResponse> {
            override fun onResponse(
                call: Call<AddEditAddressResponse>,
                response: Response<AddEditAddressResponse>
            ) {
                loader!!.dismiss()
                val addEditAddressResponse: AddEditAddressResponse = response.body()!!

                if (addEditAddressResponse.status_code == 200) {
                    Functions.showToast(this@SelectLocationActivityGOOGLEMAP, addEditAddressResponse.message)
                    finish()
                } else {
                    Functions.showToast(this@SelectLocationActivityGOOGLEMAP, addEditAddressResponse.message)
                }
            }

            override fun onFailure(call: Call<AddEditAddressResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(
                    this@SelectLocationActivityGOOGLEMAP,
                    getString(R.string.someting_want_wrong)
                )
            }
        })

    }

    private fun callAddAddress() {
        loader!!.show()

        val addAddressRequest = AddAddressRequest()
        addAddressRequest.user_id = SharedPrefManager.getUserID(this).toString()
        addAddressRequest.address_type = address_value
        addAddressRequest.name = edtAddressName.text.toString()
        addAddressRequest.address = edtHouseNo.text!!.toString()
        addAddressRequest.phone = SharedPrefManager.getUserData(this)!!.phone
        addAddressRequest.landmark = edtLandMark.text!!.toString()
        addAddressRequest.other_type = other_type

        val api = MyApplication.getRetrofit().create(ApiInterface::class.java)
        api.add_address(addAddressRequest).enqueue(object : Callback<AddEditAddressResponse> {
            override fun onResponse(
                call: Call<AddEditAddressResponse>,
                response: Response<AddEditAddressResponse>
            ) {
                loader!!.dismiss()
                val addEditAddressResponse: AddEditAddressResponse = response.body()!!

                if (addEditAddressResponse.status_code == 200) {
                    Functions.showToast(this@SelectLocationActivityGOOGLEMAP, addEditAddressResponse.message)
                    finish()
                } else {
                    Functions.showToast(this@SelectLocationActivityGOOGLEMAP, addEditAddressResponse.message)
                }
            }

            override fun onFailure(call: Call<AddEditAddressResponse>, t: Throwable) {
                loader!!.dismiss()
                Functions.showToast(
                    this@SelectLocationActivityGOOGLEMAP,
                    getString(R.string.someting_want_wrong)
                )
            }
        })

    }


    private fun getAddress(latitude: Double, longitude: Double): String? {
        val result = StringBuilder()
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses.size > 0) {
                val address = addresses[0]
                result.append(address.getAddressLine(0))//.append("\n")
                //result.append(address.countryName)
                city_name = address.locality
            }
        } catch (e: IOException) {
            Log.e("tag", e.message!!)
        }
        return result.toString()
    }


    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("Select Location")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            @Suppress("DEPRECATED_IDENTITY_EQUALS")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                === PackageManager.PERMISSION_GRANTED
            ) {

                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true

                mMap!!.setOnMapClickListener { latLng -> // Creating a marker
                    val markerOptions = MarkerOptions()

                    markerOptions.position(latLng)

                    onMapTapLat = latLng.latitude.toString()
                    onMapTapLong = latLng.longitude.toString()


                    Log.e("onMapTapLat", onMapTapLat.toString())
                    Log.e("onMapTapLong", onMapTapLong.toString())

                    markerOptions.title(latLng.latitude.toString() + " : " + latLng.longitude)
                    mMap!!.clear()

                    mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                }

            } else {

                checkLocationPermission()
            }
        } else {
            buildGoogleApiClient()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap!!.isMyLocationEnabled = true
        }


    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        mGoogleApiClient!!.connect()

    }

    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton(
                        "OK"
                    ) { _, i -> //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                            this@SelectLocationActivityGOOGLEMAP,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        val name = place.name
                        val address = place.address
                        /*   Log.e("TAG", "Place: ${place.name}, ${place.address} ,${place.id}")
                        Log.e("TALTNG", "latlong: ${place.latLng}")*/
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(place.latLng, 18F))
                        addressFinal = "$name, $address"
                        edtAddress.setText("$name, $address")

                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.e("TAG", status.statusMessage!!)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    Log.e("TAG", "RESULT_CANCELED")
                }
            }
            return
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
            )
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location?) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        val latLng = location?.latitude?.let { LatLng(it, location?.longitude) }
        latitude = location?.latitude.toString()
        longitude = location?.longitude.toString()

        Log.e("latitude", latitude!!)
        Log.e("longitude", longitude!!)


        //val markerOptions = MarkerOptions()
        //markerOptions.position(latLng!!)
        //markerOptions.title("You are here")
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
        //mCurrLocationMarker = mMap!!.addMarker(markerOptions)
        if (isFirstTime == false) {
            SharedPrefManager.setLatitude(this, latitude!!)
            SharedPrefManager.setLongitude(this, longitude!!)

            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
            val address = getAddress(latLng!!.latitude, latLng.longitude)
            addressFinal = address
            edtAddress.setText(addressFinal)

            finalLatitude = SharedPrefManager.getLatitude(this)
            finalLongitude = SharedPrefManager.getLongitude(this)


            isFirstTime = true
        }
    }

}