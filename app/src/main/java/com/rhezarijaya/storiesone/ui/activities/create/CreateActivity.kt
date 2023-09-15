package com.rhezarijaya.storiesone.ui.activities.create

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.rhezarijaya.storiesone.BuildConfig
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.databinding.ActivityCreateBinding
import com.rhezarijaya.storiesone.ui.activities.main.MainActivity
import com.rhezarijaya.storiesone.util.Helpers
import com.rhezarijaya.storiesone.util.Result
import com.rhezarijaya.storiesone.util.ViewModelFactory
import java.io.File

class CreateActivity : AppCompatActivity() {
    private val createViewModel by viewModels<CreateViewModel> {
        ViewModelFactory.getInstance(this)
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
    private val intentCameraPermissionLauncher =
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

    private lateinit var binding: ActivityCreateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = getString(R.string.create_story)
            setDisplayHomeAsUpEnabled(true)
        }

        if (!Helpers.isPermissionGranted(this, CAMERA_PERMISSION)) {
            intentCameraPermissionLauncher.launch(CAMERA_PERMISSION)
        }

        createViewModel.getImageFile()?.let {
            setPreviewImage(it)
        }

        setAddButtonEnabled()

        binding.apply {
            edAddDescription.addTextChangedListener(
                onTextChanged = { _, _, _, _ ->
                    setAddButtonEnabled()
                }
            )

            btnCamera.setOnClickListener {
                if (!Helpers.isPermissionGranted(this@CreateActivity, CAMERA_PERMISSION)) {
                    intentCameraPermissionLauncher.launch(CAMERA_PERMISSION)
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

            buttonAdd.setOnClickListener {
                createViewModel.getImageFile()?.let {
                    setInputsEnabled(false)
                    val description = edAddDescription.text.toString()

                    createViewModel.addStory(description, it, null, null)
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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
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

    private fun setPreviewImage(file: File) {
        Glide.with(this)
            .load(file)
            .placeholder(R.drawable.baseline_broken_image_24)
            .error(R.drawable.baseline_broken_image_24)
            .into(binding.ivPreview)
    }

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }
}