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
import com.example.cloud_classification_app_kotlin.ml.Efficientnetv2b0
import com.example.cloud_classification_app_kotlin.ml.Mobilenetv3small
import com.example.cloud_classification_app_kotlin.ml.Resnet50Quantized
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

    // Activity result contract for taking a picture
    private val contract = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Check if the bitmap is null (user canceled the action)
        if (bitmap != null) {
            this.bitmap = bitmap
            imageView.setImageBitmap(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perform_classification)
        setupEdgeToEdgeInsets()

        // Initialize the image URL and model name
        imageURL = createImageUri()
        modelName = intent.getStringExtra("modelName") ?: ""
        initUI()

        // Set up the toolbar with the model name
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = modelName
            setDisplayHomeAsUpEnabled(true)
        }

        // Load labels and descriptions from assets
        val labels = assets.open("labels.txt").bufferedReader().readLines()
        val descriptions = assets.open("descriptions.txt").bufferedReader().readLines()
        val imageProcessor = createImageProcessor()

        // Set up button click listeners
        selectBtn.setOnClickListener { selectImageFromGallery() }
        takePictureBtn.setOnClickListener { contract.launch(null) }
        predictBtn.setOnClickListener { predictImage(labels, descriptions, imageProcessor) }
    }

    // Sets up edge-to-edge insets for the activity
    private fun setupEdgeToEdgeInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Initializes UI elements
    private fun initUI() {
        selectBtn = findViewById(R.id.selectBtn)
        takePictureBtn = findViewById(R.id.takePictureBtn)
        predictBtn = findViewById(R.id.predictBtn)
        imageView = findViewById(R.id.imageView)
    }

    // Creates an ImageProcessor for resizing the image
    private fun createImageProcessor(): ImageProcessor {
        return ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()
    }

    // Launches an intent to select an image from the gallery
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, 100)
    }

    // Predicts the class of the image using the selected model
    private fun predictImage(labels: List<String>, descriptions: List<String>, imageProcessor: ImageProcessor) {
        if (::bitmap.isInitialized) {
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)
            tensorImage = imageProcessor.process(tensorImage)

            // Perform model inference based on the selected model name
            val modelOutputs = when (modelName) {
                "ResNet50" -> {
                    val model = Resnet50Quantized.newInstance(this)
                    val output = runModelInference(model, tensorImage)
                    model.close()
                    output
                }
                "MobileNetV3Small" -> {
                    val model = Mobilenetv3small.newInstance(this)
                    val output = runModelInference(model, tensorImage)
                    model.close()
                    output
                }
                "EfficientNetV2B0" -> {
                    val model = Efficientnetv2b0.newInstance(this)
                    val output = runModelInference(model, tensorImage)
                    model.close()
                    output
                }
                else -> return
            }

            // Find the index of the highest confidence output
            val maxIdx = modelOutputs.indices.maxByOrNull { modelOutputs[it] } ?: 0
            showMaterialAlert(this, labels[maxIdx], descriptions[maxIdx])
        }
    }

    // Runs the model inference and returns the output feature
    private fun runModelInference(model: Any, tensorImage: TensorImage): FloatArray {
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32).apply {
            loadBuffer(tensorImage.buffer)
        }
        return when (model) {
            is Resnet50Quantized -> model.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray
            is Mobilenetv3small -> model.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray
            is Efficientnetv2b0 -> model.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray
            else -> floatArrayOf()
        }
    }

    // Handles the result from the image selection activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            imageView.setImageBitmap(bitmap)
        }
    }

    // Creates a URI for the captured image
    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photos")
        return FileProvider.getUriForFile(
            this,
            "com.example.cloud_classification_app_kotlin.FileProvider",
            image
        )
    }

    // Displays a material alert dialog with the prediction result
    private fun showMaterialAlert(context: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
