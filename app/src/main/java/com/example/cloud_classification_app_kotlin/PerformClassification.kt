package com.example.cloud_classification_app_kotlin

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cloud_classification_app_kotlin.ml.Mobilenetv3small
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File

class PerformClassification : AppCompatActivity() {
    private lateinit var selectBtn: Button
    private lateinit var takePictureBtn: Button
    private lateinit var predictBtn: Button
    private lateinit var imageView: ImageView
    private lateinit var bitmap: Bitmap
    private lateinit var modelName: String
    private lateinit var imageURL: Uri

    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){
        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageURL)
        imageView.setImageBitmap(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perform_classification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageURL = createImageUri()
        modelName = intent.getStringExtra("modelName") ?: ""
        selectBtn = findViewById(R.id.selectBtn)
        takePictureBtn = findViewById(R.id.takePictureBtn)
        predictBtn = findViewById(R.id.predictBtn)
        imageView = findViewById(R.id.imageView)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = modelName // Set the title
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var labels = application.assets.open("labels.txt").bufferedReader().readLines()
        var descriptions = application.assets.open("descriptions.txt").bufferedReader().readLines()

        // image processor? - maybe not needed
        var imageProcessor = ImageProcessor
            .Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        selectBtn.setOnClickListener{
            var intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent, 100)
        }

        takePictureBtn.setOnClickListener {
            contract.launch(imageURL)
        }

        predictBtn.setOnClickListener {
            if (::bitmap.isInitialized){
                if (modelName == "ResNet50"){
                    showMaterialAlert(this, "Alert", "This is an alert message")
                } else if (modelName == "MobileNetV3Small"){
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

                    //resView.setText(labels[maxIdx])

                    // Releases model resources if no longer used.
                    model.close()

                    showMaterialAlert(this, labels[maxIdx], descriptions[maxIdx])

                } else if (modelName == "EfficientNetV2B0"){}
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

    private fun createImageUri(): Uri{
        val image = File(filesDir, "camera_photos")
        return FileProvider.getUriForFile(this,
            "com.example.cloud_classification_app_kotlin.FileProvider",
            image)
    }

    fun showMaterialAlert(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}