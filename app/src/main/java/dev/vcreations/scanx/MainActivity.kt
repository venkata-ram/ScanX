package dev.vcreations.scanx

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dev.vcreations.scanx.utils.CommonUtils.Companion.findWordEnd
import dev.vcreations.scanx.utils.CommonUtils.Companion.findWordStart
import dev.vcreations.scanx.utils.CommonUtils.Companion.rotateBitmap
import dev.vcreations.scanx.utils.CommonUtils.Companion.uriToBitmap


class MainActivity : AppCompatActivity() {
    private lateinit var cameraBtn: ImageView
    private lateinit var textFromImage: TextView
    private lateinit var instructionText: TextView
    private var imageUri: Uri? = null
    private lateinit var recognizer : TextRecognizer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        setupListener()
        initTextRecognizer()
    }

    private fun setupViews() {
        cameraBtn = findViewById(R.id.camera_image_view)
        instructionText = findViewById(R.id.instructions_tv)
        textFromImage = findViewById(R.id.text_from_image_textview)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupListener() {
        cameraBtn.setOnClickListener {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                val permission = arrayOf(android.Manifest.permission.CAMERA)
                requestPermissions(permission, 122);
            } else {
                openCamera();
            }
        }
        var isTouching = false
        textFromImage.setOnTouchListener(OnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    isTouching = true
                }
                MotionEvent.ACTION_MOVE -> {
                    isTouching = false

                }
                MotionEvent.ACTION_UP -> {
                    if(isTouching) {
                        highlightTheWord(event)
                    }
                }
            }
            true
        })
    }

    private fun initTextRecognizer() {
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }


    /**
     * opens camera so that user can capture image
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(cameraIntent)
    }

    /**
     *  process the image and retrieve text from it
     */
    private var cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val inputImage = uriToBitmap(imageUri,contentResolver)
                val rotated = rotateBitmap(inputImage,imageUri,contentResolver)
                val finalImage = InputImage.fromBitmap(rotated, 0)
                recognizer.process(finalImage)
                    .addOnSuccessListener { visionText ->
                        if(visionText.text.isEmpty())
                            textFromImage.text = getString(R.string.no_text_detected)
                        else {
                            textFromImage.text = visionText.text
                            instructionText.text = getString(R.string.click_on_a_word_to_see_top_tweets)
                        }
                    }
                    .addOnFailureListener { e ->
                        textFromImage.text = e.message
                    }
            }
        }

    private fun highlightTheWord(event : MotionEvent){
        // Get the x and y coordinates of the touch event
        val x = event.x
        val y = event.y

        val text = textFromImage.text.toString()

        // Find the offset (index) in the text where the touch event occurred
        val offset: Int = textFromImage.getOffsetForPosition(x, y)

        // Find the word boundaries (start and end indices) around the touched position
        val start: Int = findWordStart(text, offset)
        val end: Int = findWordEnd(text, offset)

        // Now, you have the start and end indices of the touched word
        // You can use them as needed
        val touchedWord: String = text.substring(start, end)

        // display the touched word
        Toast.makeText(
            applicationContext,
            touchedWord,
            Toast.LENGTH_SHORT
        ).show()

        // highlights the touched word in yellow color
        val spannableString = SpannableString(text)
        val highlightSpan = BackgroundColorSpan(Color.YELLOW)
        spannableString.setSpan(
            highlightSpan,
            start,
            end,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        textFromImage.text = spannableString
    }

}

