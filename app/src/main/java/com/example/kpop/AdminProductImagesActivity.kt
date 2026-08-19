package com.example.kpop

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminProductImageAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiProductImage
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AdminProductImagesActivity : AppCompatActivity() {

    private lateinit var adapter: AdminProductImageAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtNoImages: TextView
    private lateinit var txtSelectedFile: TextView
    private lateinit var imgSelected: ImageView
    private lateinit var btnUploadImage: Button

    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }

    private var productId = 0
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imgSelected.setImageURI(uri)
            imgSelected.visibility = View.VISIBLE
            txtSelectedFile.text = getFileName(uri)
            btnUploadImage.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_product_images)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        productId = intent.getIntExtra("productId", 0)
        val productName = intent.getStringExtra("productName") ?: "Product Images"

        if (productId == 0) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtProductName = findViewById<TextView>(R.id.txtProductName)
        val btnChooseImage = findViewById<Button>(R.id.btnChooseImage)

        recyclerView = findViewById(R.id.imageRecyclerView)
        txtNoImages = findViewById(R.id.txtNoImages)
        txtSelectedFile = findViewById(R.id.txtSelectedFile)
        imgSelected = findViewById(R.id.imgSelected)
        btnUploadImage = findViewById(R.id.btnUploadImage)

        txtProductName.text = productName

        btnBack.setOnClickListener { finish() }

        btnChooseImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnUploadImage.setOnClickListener {
            uploadImage()
        }

        adapter = AdminProductImageAdapter(emptyList()) { image ->
            confirmDelete(image)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadImages()
    }

    private fun loadImages() {
        productApi.getProductImages(productId).enqueue(object : Callback<List<ApiProductImage>> {
            override fun onResponse(call: Call<List<ApiProductImage>>, response: Response<List<ApiProductImage>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminProductImagesActivity, response.errorBody()?.string() ?: "Unable to load images", Toast.LENGTH_SHORT).show()
                    return
                }

                val images = response.body() ?: emptyList()

                adapter.updateList(images)
                recyclerView.visibility = if (images.isEmpty()) View.GONE else View.VISIBLE
                txtNoImages.visibility = if (images.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<List<ApiProductImage>>, t: Throwable) {
                Toast.makeText(this@AdminProductImagesActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun uploadImage() {
        val uri = selectedImageUri

        if (uri == null) {
            Toast.makeText(this, "Choose an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val fileName = getFileName(uri)
        val extension = getExtension(fileName, mimeType)
        val tempFile = File.createTempFile("product_", extension, cacheDir)

        try {
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: run {
                Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to read image: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        val mediaType = MediaType.parse(mimeType)
        val requestBody = RequestBody.create(mediaType, tempFile)
        val multipart = MultipartBody.Part.createFormData("file", fileName, requestBody)

        btnUploadImage.isEnabled = false

        productApi.uploadProductImage(productId, multipart).enqueue(object : Callback<ApiProductImage> {
            override fun onResponse(call: Call<ApiProductImage>, response: Response<ApiProductImage>) {
                tempFile.delete()

                if (!response.isSuccessful) {
                    btnUploadImage.isEnabled = true
                    Toast.makeText(this@AdminProductImagesActivity, response.errorBody()?.string() ?: "Unable to upload image", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminProductImagesActivity, "Image uploaded", Toast.LENGTH_SHORT).show()

                selectedImageUri = null
                imgSelected.setImageDrawable(null)
                imgSelected.visibility = View.GONE
                txtSelectedFile.text = "No image selected"
                btnUploadImage.isEnabled = false

                loadImages()
            }

            override fun onFailure(call: Call<ApiProductImage>, t: Throwable) {
                tempFile.delete()
                btnUploadImage.isEnabled = true
                Toast.makeText(this@AdminProductImagesActivity, "Upload failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "product_image.jpg"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

            if (index >= 0 && cursor.moveToFirst()) {
                fileName = cursor.getString(index)
            }
        }

        return fileName
    }

    private fun getExtension(fileName: String, mimeType: String): String {
        if (fileName.contains(".")) return "." + fileName.substringAfterLast(".")

        return when (mimeType) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            else -> ".jpg"
        }
    }

    private fun confirmDelete(image: ApiProductImage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Delete this product image?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                deleteImage(image.id)
            }
            .show()
    }

    private fun deleteImage(imageId: Int) {
        productApi.deleteProductImage(imageId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminProductImagesActivity, response.errorBody()?.string() ?: "Unable to delete image", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminProductImagesActivity, "Image deleted", Toast.LENGTH_SHORT).show()
                loadImages()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminProductImagesActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}