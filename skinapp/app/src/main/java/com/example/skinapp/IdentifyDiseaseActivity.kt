package com.example.skinapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlin.random.Random

class IdentifyDiseaseActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val GALLERY_REQUEST_CODE = 200
    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView

    // List of diseases with name
    private val diseases = listOf(
        "Actinic Keratosis",
        "Basal Cell Carcinoma",
        "Dermatofibroma",
        "Melanoma",
        "Nevus",
        "Pigmented Benign Keratosis",
        "Seborrheic Keratosis",
        "Squamous Cell Carcinoma",
        "Vascular Lesion"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identify_disease)

        val btnOpenCamera = findViewById<Button>(R.id.btn_open_camera)
        val btnSelectFromGallery = findViewById<Button>(R.id.btn_select_from_gallery)
        val btnCalculate = findViewById<Button>(R.id.btn_calculate)
        imageView = findViewById(R.id.image_view)
        resultTextView = findViewById(R.id.text_view_result)

        // Handle Camera button click
        btnOpenCamera.setOnClickListener {
            if (checkAndRequestPermissions()) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        }

        // Handle Select from Gallery button click
        btnSelectFromGallery.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        // Handle Calculate button click
        btnCalculate.setOnClickListener {
            displayRandomDiseaseAndAccuracy()
        }
    }

    // Function to display random skin disease and accuracy
    private fun displayRandomDiseaseAndAccuracy() {
        // Pick a random disease from the list
        val randomDisease = diseases.random()

        // Generate a random accuracy between 50 and 100
        val randomAccuracy = Random.nextInt(50, 101)

        // Display the result in the TextView
        resultTextView.text = "Disease: $randomDisease\nAccuracy: $randomAccuracy%"
    }

    // Check and request permissions
    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = mutableListOf<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }
        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), CAMERA_REQUEST_CODE)
            return false
        }
        return true
    }

    // Handle the result of Camera or Gallery intent
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(imageBitmap) // Display the captured image
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri: Uri? = data?.data
                    imageView.setImageURI(selectedImageUri) // Display the selected image from gallery
                }
            }
        }
    }

    // Handle runtime permission result
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
