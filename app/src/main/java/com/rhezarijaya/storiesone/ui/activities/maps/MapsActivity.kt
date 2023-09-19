package com.rhezarijaya.storiesone.ui.activities.maps

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityMapsBinding
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory

@ExperimentalPagingApi
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val mapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                enableMyLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.maps)
            setDisplayHomeAsUpEnabled(true)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh_map -> {
                loadMarkers()
                true
            }

            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.apply {
            isCompassEnabled = true
            isIndoorLevelPickerEnabled = false
            isMapToolbarEnabled = true
            isZoomControlsEnabled = true
        }

        try {
            mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_theme_dark)
            )
        } catch (_: Resources.NotFoundException) {
        }

        enableMyLocation()
        loadMarkers()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (Helpers.isPermissionGranted(this, LOCATION_PERMISSION)) {
            mMap.isMyLocationEnabled = true
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSION)
        }
    }

    private fun loadMarkers() {
        mapsViewModel.getStories().observe(this) {
            when (it) {
                is Result.Success -> {
                    setLoadingVisible(false)

                    val bounds = LatLngBounds.Builder()
                    mMap.clear()

                    it.data.listStory.forEach { story ->
                        story.lat?.let { lat ->
                            story.lon?.let { lon ->
                                val latLng = LatLng(lat, lon)

                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(story.name)
                                        .snippet(getString(R.string.coordinate_format, lat, lon))
                                )
                                bounds.include(latLng)
                            }
                        }
                    }

                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(bounds.build(), 60)
                    )
                }

                is Result.Loading -> {
                    setLoadingVisible(true)
                    mMap.clear()
                }

                is Result.Error -> {
                    setLoadingVisible(false)
                    Toast.makeText(
                        this,
                        getString(R.string.show_markers_fail), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    companion object {
        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }
}