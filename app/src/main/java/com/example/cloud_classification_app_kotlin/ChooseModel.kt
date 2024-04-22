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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var ResNet50Btn: Button = findViewById(R.id.ResNet50Btn)
        var MobileNetV3SmallBtn: Button = findViewById(R.id.MobileNetV3SmallBtn)
        var EfficientNetV2B0Btn: Button = findViewById(R.id.EfficientNetV2B0Btn)

        ResNet50Btn.setOnClickListener {
            chooseModelForClassification("ResNet50")
        }

        MobileNetV3SmallBtn.setOnClickListener {
            chooseModelForClassification("MobileNetV3Small")
        }

        EfficientNetV2B0Btn.setOnClickListener {
            chooseModelForClassification("EfficientNetV2B0")
        }
    }

    private fun chooseModelForClassification(modelName: String) {
        val intent = Intent(this, PerformClassification::class.java)
        intent.putExtra("modelName", modelName)
        startActivity(intent)
    }
}