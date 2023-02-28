package com.maoungedev.textrecognationapplication

import android.Manifest
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.maoungedev.textrecognationapplication.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var getUriFile: Uri? = null
    private lateinit var image: InputImage

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        binding.apply {
            btnTakeImage.setOnClickListener {
                takeImageFromGallery()
            }

            btnStartML.setOnClickListener {
                getInputImage()
            }

            btnSNextPage.setOnClickListener {
                Intent(this@MainActivity, SecondActivity::class.java).also {
                    startActivity(it)
                }
            }
        }


    }

    private fun getInputImage() {

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        try {
            image = InputImage.fromFilePath(this, getUriFile!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        recognizer.process(image).addOnSuccessListener { visionText ->
            showDebugConsole("SUCCESS TEXT VISION", visionText.text)
            processTextRecognition(visionText)
        }.addOnFailureListener { e ->
            Log.e("ERROR ML KIT", e.message.toString())
        }


    }

    private fun processTextRecognition(visionText: Text) {
        val resultText = visionText.text
        val db = Firebase.firestore

        val data = hashMapOf(
            "result" to resultText
        )

        db.collection("text_recognition").document("text").set(data)
            .addOnSuccessListener {
            showDebugConsole("ADD SUCCESS", "Berhasil")
        }.addOnFailureListener {
            showDebugConsole("ERROR DB", it.message.toString())
        }
    }

    private fun showDebugConsole(tag: String, message: String) {
        Log.d(tag, message)
    }

    private fun takeImageFromGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            uriToFile(selectedImg, this@MainActivity)

            getUriFile = selectedImg

            binding.imageView.setImageURI(selectedImg)
        }
    }

    companion object {

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}