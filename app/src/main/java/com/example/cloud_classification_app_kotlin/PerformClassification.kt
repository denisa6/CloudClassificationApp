package com.example.cloud_classification_app_kotlin

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cloud_classification_app_kotlin.ml.Mobilenetv3small
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class PerformClassification : AppCompatActivity() {
    lateinit var selectBtn: Button
    lateinit var predictBtn: Button
    lateinit var backBtn: Button
    lateinit var resView: TextView
    lateinit var imageView: ImageView
    lateinit var bitmap: Bitmap
    lateinit var modelName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perform_classification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        modelName = intent.getStringExtra("modelName") ?: ""

        selectBtn = findViewById(R.id.selectBtn)
        predictBtn = findViewById(R.id.predictBtn)
        backBtn = findViewById(R.id.backBtn)
        resView = findViewById(R.id.resultView)
        imageView = findViewById(R.id.imageView)

        resView.setText(modelName)

        var labels = application.assets.open("labels.txt").bufferedReader().readLines()

        // image processor? - maybe not needed
        var imageProcessor = ImageProcessor
            .Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        backBtn.setOnClickListener {
            val intent = Intent(this, ChooseModel::class.java)
            startActivity(intent)
        }

        selectBtn.setOnClickListener{
            var intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, 100)
        }

        predictBtn.setOnClickListener {
            if (modelName == "ResNet50"){

            }else if (modelName == "MobileNetV3Small"){
                var tensorImage = TensorImage(DataType.FLOAT32)
                tensorImage.load(bitmap)

                // use image processor
                tensorImage = imageProcessor.process(tensorImage)

                val model = Mobilenetv3small.newInstance(this)

                // Creates inputs for reference.
                val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
                inputFeature0.loadBuffer(tensorImage.buffer)

                // Runs model inference and gets result.
                val outputs = model.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer.floatArray

                var maxIdx = 0
                outputFeature0.forEachIndexed { index, fl ->
                    if (outputFeature0[maxIdx] < fl){
                        maxIdx = index
                    }
                }

                resView.setText(labels[maxIdx])

                // Releases model resources if no longer used.
                model.close()
            } else if (modelName == "EfficientNetV2B0"){

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 100){
            var uri = data?.data
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageView.setImageBitmap(bitmap)
        }
    }
}