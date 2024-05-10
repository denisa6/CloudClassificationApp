package com.example.cloud_classification_app_kotlin

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DisplayResult : AppCompatActivity() {
    private lateinit var resView: TextView
    private lateinit var descriptionView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_display_result)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.displayToolbar))
        supportActionBar?.title = "Prediction Result" // Set the title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        resView = findViewById(R.id.resultView)
        descriptionView = findViewById(R.id.descriptionView)
        imageView = findViewById(R.id.imageDisplayView)

        var cloudType = intent.getStringExtra("cloudType") ?: ""
        resView.setText(cloudType)
    }
}