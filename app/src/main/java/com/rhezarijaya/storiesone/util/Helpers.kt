package com.rhezarijaya.storiesone.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.util.Patterns
import android.widget.Toast
import androidx.camera.core.impl.utils.Exif
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.rhezarijaya.storiesone.BuildConfig
import com.rhezarijaya.storiesone.R
import com.rhezarijaya.storiesone.data.network.response.BaseResponse
import retrofit2.HttpException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

object Helpers {
    @SuppressLint("RestrictedApi")
    fun adjustImageRotation(file: File) {
        // rotasi diambil otomatis dengan metadata exif
        val exifData = Exif.createFromFile(file)
        val rotation = exifData.rotation

        val bitmapFile = BitmapFactory.decodeFile(file.path)
        val matrix = Matrix().apply {
            postRotate(rotation.toFloat())
        }

        Bitmap.createBitmap(bitmapFile, 0, 0, bitmapFile.width, bitmapFile.height, matrix, true)
            .compress(Bitmap.CompressFormat.JPEG, 100, FileOutputStream(file))
    }

    fun apiDateFormatter(dateString: String): String {
        // 2022-01-08T06:34:18.598Z
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
            sdf.timeZone = TimeZone.getTimeZone("GMT") // karena Z adalah timezone GMT
            sdf.parse(dateString)?.let {
                DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.DEFAULT).format(it)
            } ?: "-"
        } catch (e: Exception) {
            "-"
        }
    }

    fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)

        var quality = 100
        var currentSize: Int

        do {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)

            currentSize = byteArrayOutputStream.size()
            quality -= 5
        } while (currentSize > Constants.IMAGE_MAX_SIZE)

        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, FileOutputStream(file))
        return file
    }

    fun createTempImageFile(context: Context): File =
        File.createTempFile(
            "temp${Random.nextLong(100, 999)}_${System.currentTimeMillis()}",
            ".jpg",
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )

    fun isEmailValid(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordValid(password: String) = password.length >= 8

    fun isPermissionGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun readImageFromUri(context: Context, uri: Uri): File {
        val file = createTempImageFile(context)

        try {
            val inputStream = context.contentResolver.openInputStream(uri)!!
            val outputStream = FileOutputStream(file)

            val bufffer = ByteArray(1024)
            var length: Int

            while (inputStream.read(bufffer).also { length = it } > 0) {
                outputStream.write(bufffer, 0, length)
            }

            outputStream.close()
            inputStream.close()
        } catch (_: Exception) {
        }

        return file
    }

    fun retrofitExceptionHandler(context: Context, exception: Exception) {
        if (BuildConfig.DEBUG) {
            exception.printStackTrace()
        }

        if (exception is HttpException) {
            val message = exception.response()?.errorBody()?.string()?.let { errorBody ->
                // data response ketika error perlu didapat dari errorBody dengan string()
                // karena toString() menghasilkan string alamat objek di memori
                // lalu di parse menjadi json object dengan bentuk BaseResponse (error dan message)
                try {
                    Gson().fromJson(errorBody, BaseResponse::class.java).message
                } catch (e: Exception) {
                    context.getString(R.string.unexpected_error)
                }
            } ?: run {
                context.getString(R.string.unexpected_error)
            }

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.unexpected_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}