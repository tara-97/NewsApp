package com.example.android.newsapp

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.android.newsapp.databinding.ActivityMainBinding
import com.example.android.newsapp.network.NewsCountryFilter
import com.example.android.newsapp.ui.NewsAdapter
import com.example.android.newsapp.ui.OnClickListener
import com.example.android.newsapp.viewmodel.NewsViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


const val EXTRA_URL = "News_url"
private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private val viewModel: NewsViewModel by lazy {
        val activity = requireNotNull(this)
        val factory = NewsViewModel.Factory(activity.application)
        ViewModelProvider(this, factory).get(NewsViewModel::class.java)
    }
    private lateinit var  fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = false
    private var lastUserCounty: String? = null
    private lateinit var adLoader : AdLoader
    private lateinit var binding : ActivityMainBinding
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private lateinit var  remoteConfig : FirebaseRemoteConfig
    private var isAdEnabled : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= DataBindingUtil.setContentView(
            this, R.layout.activity_main
        )
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        val defaults : Map<String,Boolean> = mapOf(IS_AD_SERVICE_ENABLED
                to true)
        remoteConfig.setDefaultsAsync(defaults)

        MobileAds.initialize(this)
        updateAdsLoadPermission()



        fusedLocationProviderClient = FusedLocationProviderClient(this)
        getLocationPermission()

        binding.viewModel = viewModel
        val adapter = NewsAdapter(OnClickListener { 

                val intent = Intent(this, DetailActivity::class.java).apply {
                    putExtra(EXTRA_URL, it)
                }
            startActivity(intent)
        })


        binding.newsItemList.adapter = adapter
        viewModel.newsLive.observe(this, Observer {
            if(null != it && it.isNotEmpty()){
                binding.statusImage.visibility = View.GONE
                binding.myTemplate.visibility = View.VISIBLE
                adapter.submitList(it)
            }else{
                binding.statusImage.visibility = View.VISIBLE
                binding.myTemplate.visibility = View.INVISIBLE
            }
        })





    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.refresh_menu_item ->{
                refreshData()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        when{
            ContextCompat.checkSelfPermission(this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                locationPermissionGranted = true

            }

            else -> {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            }
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true


                }
            }
        }
        updateLocation()
    }
    private fun updateLocation() {
        if(!locationPermissionGranted){
            lastUserCounty = null
            getLocationPermission()

        }else{
            getDeviceLocationAndUpdateNews()
        }

    }
    private fun getDeviceLocationAndUpdateNews() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

                if (locationPermissionGranted) {
                    val locationResult = fusedLocationProviderClient.lastLocation
                    locationResult.addOnCompleteListener(this) { task ->

                            if (task.isSuccessful) {
                                val lastKnownLocation = task.result
                                if (lastKnownLocation != null) {
                                    uiScope.launch {
                                    val address = getUserCountryAsync(lastKnownLocation)
                                    lastUserCounty = address[0].countryName
                                    updateNewsResultForLocation(lastUserCounty)

                                    }

                                }


                            }
                    }
                }else{
                    Toast.makeText(this,"Pls Give Location Permision",Toast.LENGTH_LONG).show()
                    uiScope.launch {
                        updateNewsResultForLocation(lastUserCounty)
                    }

                }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    private suspend fun getUserCountryAsync(lastKnownLocation:Location) : List<Address> {
        val address = withContext(Default){
            val geocoder = Geocoder(applicationContext, Locale.getDefault())
            val address = geocoder.getFromLocation(
                lastKnownLocation.latitude,
                    lastKnownLocation.longitude,1)
            return@withContext address

        }
        return address
    }

    private suspend fun updateNewsResultForLocation(country:String?){
        country?.let {
            when(country){
                "India" -> viewModel.getRecentLiveNews(NewsCountryFilter.COUNTRY_IND)
                else -> viewModel.getRecentLiveNews(NewsCountryFilter.COUNTRY_USA)
            }
        }

    }

    private fun updateAdsLoadPermission(){
        remoteConfig.fetchAndActivate()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        isAdEnabled = remoteConfig.getBoolean(IS_AD_SERVICE_ENABLED)


                    } else {
                        isAdEnabled = true
                    }
                    loadNativeAds()
                }

    }



    /* Load Native Ad to Show
     *If Adservice is Enabled
     */
    private fun loadNativeAds(){
        if(isAdEnabled){

            adLoader = AdLoader.Builder(this, "ca-app-pub-3940256099942544/2247696110")
                    .forUnifiedNativeAd { ad : UnifiedNativeAd ->
                        // Show the ad.
                        if (isDestroyed) {
                            ad.destroy()
                            return@forUnifiedNativeAd
                        }
                        binding.myTemplate.setNativeAd(ad)



                    }
                    .withAdListener(object : AdListener() {
                        override fun onAdFailedToLoad(adError: LoadAdError) {
                            // Handling Code for Failing To Load Ad
                        }
                    })
                    .withNativeAdOptions(
                            NativeAdOptions.Builder()
                                    // Methods in the NativeAdOptions.Builder class can be
                                    // used here to specify individual options settings.
                                    .build())
                    .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }else{
            binding.myTemplate.visibility = View.GONE
        }

    }
    private fun refreshData(){
        lastUserCounty?.let {
            uiScope.launch {
                updateNewsResultForLocation(lastUserCounty)
            }
        }
        loadNativeAds()

    }




    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val IS_AD_SERVICE_ENABLED = "is_ad_service_enabled"
    }



}

