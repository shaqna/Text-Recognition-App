package com.maoungedev.textrecognationapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.maoungedev.textrecognationapplication.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {

    private val binding: ActivitySecondBinding by lazy {
        ActivitySecondBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val db = Firebase.firestore

        db.collection("text_recognition").document("text").get().addOnSuccessListener {
            if(it.exists()) {
                binding.editTextText.setText(it.data?.get("result").toString())
            }
        }.addOnFailureListener {

        }

    }
}