package com.example.cloud_classification_app_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ChooseModel : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_model)
        setupEdgeToEdgeInsets()

        // Initialize buttons
        val resNet50Btn: Button = findViewById(R.id.ResNet50Btn)
        val mobileNetV3SmallBtn: Button = findViewById(R.id.MobileNetV3SmallBtn)
        val efficientNetV2B0Btn: Button = findViewById(R.id.EfficientNetV2B0Btn)

        // Set click listeners for each button to choose a model
        resNet50Btn.setOnClickListener { chooseModelForClassification("ResNet50") }
        mobileNetV3SmallBtn.setOnClickListener { chooseModelForClassification("MobileNetV3Small") }
        efficientNetV2B0Btn.setOnClickListener { chooseModelForClassification("EfficientNetV2B0") }
    }

    // Sets up edge-to-edge insets for the activity
    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Starts the PerformClassification activity with the selected model
    private fun chooseModelForClassification(modelName: String) {
        val intent = Intent(this, PerformClassification::class.java).apply {
            putExtra("modelName", modelName)
        }
        startActivity(intent)
    }
}
