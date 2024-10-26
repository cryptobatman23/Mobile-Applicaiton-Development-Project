package com.example.skinapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {


    private val diseases = listOf(
        Disease("Actinic Keratosis", "Actinic keratosis is a rough, scaly skin patch caused by prolonged sun exposure, primarily affecting sun-exposed areas. It can potentially progress to skin cancer if untreated.", R.drawable.actinic),
        Disease("Basal cell carcinoma","Basal cell carcinoma is a common type of skin cancer that develops due to long-term sun exposure, typically affecting areas like the face and neck. It grows slowly and rarely spreads but can cause damage to surrounding tissues if left untreated.", R.drawable.basalcell),
        Disease("Dermatofibroma", " A benign, firm skin nodule often caused by minor injuries like insect bites or shaving cuts, commonly found on the legs or arms.", R.drawable.dermatofibroma),
        Disease("Melanoma", " A serious type of skin cancer that develops in melanocytes (pigment-producing cells) and is often caused by intense sun exposure or UV radiation, with potential to spread rapidly.", R.drawable.melanoma),
        Disease("Nevus", " A mole or birthmark made up of clusters of melanocytes, usually harmless but can sometimes develop into melanoma.", R.drawable.nevus),
        Disease("Pigmented Benign Keratosis", " A harmless, pigmented skin growth often seen in older adults, caused by a buildup of keratin, typically on sun-exposed areas.", R.drawable.pigmented),
        Disease("Seborrheic Keratosis", " A common, benign skin growth that appears as a waxy, scaly bump, often developing with age and unrelated to sun exposure.\n" +
                "\n", R.drawable.seborrheic),
        Disease("Squamous Cell Carcinoma", " A type of skin cancer caused by long-term sun exposure, often appearing as a red, scaly patch or sore, primarily affecting sun-exposed areas.", R.drawable.squamouscell),
        Disease("Vascular Lesion", " An abnormal growth or mark on the skin caused by blood vessels, which can be congenital (like hemangiomas) or develop later in life.", R.drawable.vascularlesion)
    )

    private lateinit var interpreter: Interpreter
    private val MODEL_FILE = "final_model.tflite"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.diseaseRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = DiseaseAdapter(diseases)

        // Set up logo
        val logoImageView = findViewById<ImageView>(R.id.logoImageView)
        logoImageView.setImageResource(R.drawable.skinology)

        // Set up dropdown menu
        val dropdownMenuIcon = findViewById<ImageView>(R.id.dropdownMenuIcon)
        dropdownMenuIcon.setOnClickListener { showPopupMenu(it) }


        try {
            interpreter = Interpreter(loadModelFile(MODEL_FILE))
        } catch (e: IOException) {
            e.printStackTrace()

        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.dropdown_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_identify -> {
                    val intent = Intent(this, IdentifyDiseaseActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.action_logout -> {
                    val intent = Intent(this, SignInActivity::class.java)
                    startActivity(intent)
                    finish() // Close current activity
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Load the TensorFlow Lite model from the assets folder
    @Throws(IOException::class)
    private fun loadModelFile(modelFile: String): MappedByteBuffer {
        val assetFileDescriptor = assets.openFd(modelFile)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    private fun preprocessImage(bitmap: Bitmap): Array<FloatArray> {

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)


        val input = Array(1) { FloatArray(224 * 224 * 3) }
        var index = 0


        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                input[0][index++] = (pixel shr 16 and 0xFF) / 255.0f  // Red
                input[0][index++] = (pixel shr 8 and 0xFF) / 255.0f   // Green
                input[0][index++] = (pixel and 0xFF) / 255.0f         // Blue
            }
        }
        return input
    }
}

data class Disease(val name: String, val description: String, val imageRes: Int)

class DiseaseAdapter(private val diseases: List<Disease>) : RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder>() {

    class DiseaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val diseaseName: TextView = view.findViewById(R.id.diseaseName)
        val diseaseDescription: TextView = view.findViewById(R.id.diseaseDescription)
        val diseaseImage: ImageView = view.findViewById(R.id.diseaseImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiseaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_disease, parent, false)
        return DiseaseViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiseaseViewHolder, position: Int) {
        val disease = diseases[position]
        holder.diseaseName.text = disease.name
        holder.diseaseDescription.text = disease.description
        holder.diseaseImage.setImageResource(disease.imageRes)
    }

    override fun getItemCount(): Int = diseases.size
}
