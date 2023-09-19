package com.rhezarijaya.storiesone.ui.activities.create

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.paging.ExperimentalPagingApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.rhezarijaya.storiesone.BuildConfig
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityCreateBinding
import com.rhezarijaya.storiesone.ui.activities.main.MainActivity
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory
import com.rhezarijaya.storiesone.util.loadImage
import java.io.File

@ExperimentalPagingApi
class CreateActivity : AppCompatActivity() {
    private val createViewModel by viewModels<CreateViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_granted), Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.camera_permission_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions[LOCATION_PERMISSIONS[0]] ?: false -> {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                    getCurrentLocation()
                }

                permissions[LOCATION_PERMISSIONS[1]] ?: false -> {
                    Toast.makeText(
                        this,
                        getString(R.string.fine_location_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Toast.makeText(
                        this,
                        getString(R.string.location_permission_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    private val intentCameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                createViewModel.cameraImageFilepath?.let { path ->
                    createViewModel.setImageFile(File(path))
                    createViewModel.getImageFile()?.let { file ->
                        setPreviewImage(file)
                    }

                    setAddButtonEnabled()
                }
            }
        }
    private val intentGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val imageUri = it.data?.data!!
                val imageFileGallery = Helpers.readImageFromUri(this, imageUri)

                createViewModel.setImageFile(imageFileGallery)
                createViewModel.getImageFile()?.let { file ->
                    setPreviewImage(file)
                }

                setAddButtonEnabled()
            }
        }

    private val locationCallback = object : LocationCallback() {}
    private val locationRequest = LocationRequest.Builder(1500)
        .setMaxUpdateDelayMillis(1500)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private lateinit var binding: ActivityCreateBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.create_story)
            setDisplayHomeAsUpEnabled(true)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Helpers.isPermissionGranted(this, CAMERA_PERMISSION)) {
            cameraPermissionLauncher.launch(CAMERA_PERMISSION)
        }

        if (!Helpers.isPermissionGranted(this, LOCATION_PERMISSIONS)) {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        } else {
            getCurrentLocation(true)
        }

        createViewModel.getImageFile()?.let {
            setPreviewImage(it)
        }

        setAddButtonEnabled()

        binding.apply {
            tvLocation.text = getString(R.string.no_location)

            edAddDescription.addTextChangedListener(
                onTextChanged = { _, _, _, _ ->
                    setAddButtonEnabled()
                }
            )

            btnCamera.setOnClickListener {
                if (!Helpers.isPermissionGranted(this@CreateActivity, CAMERA_PERMISSION)) {
                    cameraPermissionLauncher.launch(CAMERA_PERMISSION)
                    return@setOnClickListener
                }

                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                val tempImage = Helpers.createTempImageFile(this@CreateActivity)
                val tempImageUri = FileProvider.getUriForFile(
                    this@CreateActivity,
                    BuildConfig.APPLICATION_ID,
                    tempImage
                )

                createViewModel.cameraImageFilepath = tempImage.absolutePath

                intent.resolveActivity(packageManager)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)

                intentCameraLauncher.launch(intent)
            }

            btnGallery.setOnClickListener {
                intentGalleryLauncher.launch(
                    Intent.createChooser(
                        Intent(Intent.ACTION_GET_CONTENT).apply {
                            type = "image/*"
                        },
                        getString(R.string.choose_an_image)
                    )
                )
            }

            switchLocation.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (!Helpers.isPermissionGranted(this@CreateActivity, LOCATION_PERMISSIONS)) {
                        locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
                        switchLocation.isChecked = false
                    } else {
                        setLocationTextVisible(true)
                    }
                } else {
                    setLocationTextVisible(false)
                }
            }

            btnUpdateLocation.setOnClickListener {
                getCurrentLocation()
            }

            buttonAdd.setOnClickListener {
                createViewModel.getImageFile()?.let {
                    val description = edAddDescription.text.toString()
                    val latLng = if (switchLocation.isChecked) {
                        createViewModel.latLng ?: run {
                            Toast.makeText(
                                this@CreateActivity,
                                getString(R.string.location_not_found),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@setOnClickListener
                        }
                    } else {
                        null
                    }

                    setInputsEnabled(false)
                    createViewModel.addStory(description, it, latLng?.latitude, latLng?.longitude)
                        .observe(this@CreateActivity) { result ->
                            when (result) {
                                is Result.Success -> {
                                    setLoadingVisible(false)
                                    Toast.makeText(
                                        this@CreateActivity,
                                        getString(R.string.post_creation_success),
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    setResult(MainActivity.CREATE_POST_RESULT_CODE)
                                    finish()
                                }

                                is Result.Loading -> {
                                    setLoadingVisible(true)
                                }

                                is Result.Error -> {
                                    result.exception.getData()?.let { exception ->
                                        setLoadingVisible(false)
                                        setInputsEnabled(true)
                                        Helpers.retrofitExceptionHandler(
                                            this@CreateActivity,
                                            exception
                                        )
                                    }
                                }
                            }
                        }
                } ?: run {
                    Toast.makeText(
                        this@CreateActivity,
                        getString(R.string.image_and_desc_required),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        stopLocationUpdates()
        super.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(isStartup: Boolean = false) {
        if (Helpers.isPermissionGranted(this, LOCATION_PERMISSIONS)) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)

                    createViewModel.latLng = latLng
                    binding.tvLocation.text =
                        getString(R.string.coordinate_format, latLng.latitude, latLng.longitude)
                } ?: run {
                    if (!isStartup) {
                        Toast.makeText(
                            this@CreateActivity,
                            getString(R.string.location_disablec),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    private fun setAddButtonEnabled() {
        binding.buttonAdd.isEnabled =
            createViewModel.getImageFile() != null && binding.edAddDescription.text.toString()
                .isNotEmpty()
    }

    private fun setInputsEnabled(isEnabled: Boolean) {
        binding.btnCamera.isEnabled = isEnabled
        binding.btnGallery.isEnabled = isEnabled
        binding.edAddDescription.isEnabled = isEnabled
        binding.buttonAdd.isEnabled = isEnabled
    }

    private fun setLoadingVisible(isVisible: Boolean) {
        binding.progressBar.isVisible = isVisible
    }

    private fun setLocationTextVisible(isVisible: Boolean) {
        binding.tvLocation.isVisible = isVisible
        binding.btnUpdateLocation.isVisible = isVisible
    }

    private fun setPreviewImage(file: File) {
        binding.ivPreview.loadImage(file)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (Helpers.isPermissionGranted(this, LOCATION_PERMISSIONS)) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private val LOCATION_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}