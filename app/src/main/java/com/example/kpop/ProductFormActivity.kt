package com.example.kpop

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CategoryApi
import com.example.kpop.network.api.GroupApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiCategory
import com.example.kpop.network.model.ApiGroup
import com.example.kpop.network.model.ApiProduct
import com.example.kpop.network.model.ApiProductImage
import com.example.kpop.network.model.ProductRequest
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProductFormActivity : AppCompatActivity() {

    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }
    private val categoryApi by lazy { RetrofitClient.create(this, CategoryApi::class.java) }
    private val groupApi by lazy { RetrofitClient.create(this, GroupApi::class.java) }

    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etQuantity: EditText
    private lateinit var etDescription: EditText
    private lateinit var etSalePrice: EditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var spinnerGroup: Spinner
    private lateinit var switchSale: Switch
    private lateinit var btnSave: Button
    private lateinit var imgPreview: ImageView
    private lateinit var txtSelectedImage: TextView

    private var productId = 0
    private var productToEdit: ApiProduct? = null
    private var categories: List<ApiCategory> = emptyList()
    private var groups: List<ApiGroup> = emptyList()
    private var selectedImageUri: Uri? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imgPreview.setImageURI(uri)
            txtSelectedImage.text = getFileName(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_product)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        productId = intent.getIntExtra("productId", 0)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtFormTitle)
        val btnChooseImage = findViewById<Button>(R.id.btnChooseImage)

        etName = findViewById(R.id.etProductName)
        etPrice = findViewById(R.id.etProductPrice)
        etQuantity = findViewById(R.id.etProductQuantity)
        etDescription = findViewById(R.id.etProductDescription)
        etSalePrice = findViewById(R.id.etProductSalePrice)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        spinnerGroup = findViewById(R.id.spinnerGroup)
        switchSale = findViewById(R.id.switchProductSale)
        btnSave = findViewById(R.id.btnSaveProduct)
        imgPreview = findViewById(R.id.imgProductPreview)
        txtSelectedImage = findViewById(R.id.txtSelectedImage)

        txtTitle.text = if (productId == 0) "Add Product" else "Edit Product"

        btnBack.setOnClickListener { finish() }
        btnChooseImage.setOnClickListener { imagePicker.launch("image/*") }

        switchSale.setOnCheckedChangeListener { _, checked ->
            etSalePrice.isEnabled = checked
            if (!checked) etSalePrice.error = null
        }

        etSalePrice.isEnabled = false
        btnSave.setOnClickListener { saveProduct() }

        loadCategories()
        loadGroups()

        if (productId != 0) loadProduct()
    }

    private fun loadCategories() {
        categoryApi.getCategories().enqueue(object : Callback<List<ApiCategory>> {
            override fun onResponse(call: Call<List<ApiCategory>>, response: Response<List<ApiCategory>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@ProductFormActivity, "Unable to load categories", Toast.LENGTH_SHORT).show()
                    return
                }

                categories = response.body() ?: emptyList()
                spinnerCategory.adapter = ArrayAdapter(this@ProductFormActivity, android.R.layout.simple_spinner_item, categories.map { it.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                applyProductSelection()
            }

            override fun onFailure(call: Call<List<ApiCategory>>, t: Throwable) {
                Toast.makeText(this@ProductFormActivity, "Category error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadGroups() {
        groupApi.getGroups().enqueue(object : Callback<List<ApiGroup>> {
            override fun onResponse(call: Call<List<ApiGroup>>, response: Response<List<ApiGroup>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@ProductFormActivity, "Unable to load groups", Toast.LENGTH_SHORT).show()
                    return
                }

                groups = response.body() ?: emptyList()
                spinnerGroup.adapter = ArrayAdapter(this@ProductFormActivity, android.R.layout.simple_spinner_item, groups.map { it.name }).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }

                applyProductSelection()
            }

            override fun onFailure(call: Call<List<ApiGroup>>, t: Throwable) {
                Toast.makeText(this@ProductFormActivity, "Group error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadProduct() {
        productApi.getProduct(productId).enqueue(object : Callback<ApiProduct> {
            override fun onResponse(call: Call<ApiProduct>, response: Response<ApiProduct>) {
                val product = response.body()

                if (!response.isSuccessful || product == null) {
                    Toast.makeText(this@ProductFormActivity, "Unable to load product", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                productToEdit = product

                etName.setText(product.name)
                etPrice.setText(product.price.toString())
                etQuantity.setText(product.quantity.toString())
                etDescription.setText(product.description ?: "")
                switchSale.isChecked = product.onSale
                etSalePrice.isEnabled = product.onSale
                etSalePrice.setText(product.salePrice?.toString() ?: "")

                if (!product.image.isNullOrBlank()) {
                    Glide.with(this@ProductFormActivity)
                        .load(RetrofitClient.imageUrl(product.image))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imgPreview)

                    txtSelectedImage.text = "Current product image"
                }

                applyProductSelection()
            }

            override fun onFailure(call: Call<ApiProduct>, t: Throwable) {
                Toast.makeText(this@ProductFormActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun applyProductSelection() {
        val product = productToEdit ?: return

        if (categories.isNotEmpty() && product.category != null) {
            val index = categories.indexOfFirst { it.id == product.category.id }
            if (index >= 0) spinnerCategory.setSelection(index)
        }

        if (groups.isNotEmpty() && product.group != null) {
            val index = groups.indexOfFirst { it.id == product.group.id }
            if (index >= 0) spinnerGroup.setSelection(index)
        }
    }

    private fun saveProduct() {
        val name = etName.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull()
        val quantity = etQuantity.text.toString().toIntOrNull()
        val description = etDescription.text.toString().trim()
        val onSale = switchSale.isChecked
        val salePrice = etSalePrice.text.toString().toDoubleOrNull()

        if (name.isEmpty()) {
            etName.error = "Enter product name"
            return
        }

        if (price == null || price <= 0) {
            etPrice.error = "Enter valid price"
            return
        }

        if (quantity == null || quantity < 0) {
            etQuantity.error = "Enter valid quantity"
            return
        }

        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show()
            return
        }

        if (groups.isEmpty()) {
            Toast.makeText(this, "No groups available", Toast.LENGTH_SHORT).show()
            return
        }

        if (onSale && (salePrice == null || salePrice <= 0 || salePrice >= price)) {
            etSalePrice.error = "Sale price must be lower than normal price"
            return
        }

        val request = ProductRequest(
            name = name,
            price = price,
            onSale = onSale,
            salePrice = if (onSale) salePrice else null,
            description = description.ifEmpty { null },
            rating = productToEdit?.rating ?: 0.0,
            quantity = quantity,
            image = productToEdit?.image,
            category = categories[spinnerCategory.selectedItemPosition],
            group = groups[spinnerGroup.selectedItemPosition]
        )

        btnSave.isEnabled = false

        if (productId == 0) {
            productApi.addProduct(request).enqueue(saveCallback(request, "Product added"))
        } else {
            productApi.updateProduct(productId, request).enqueue(saveCallback(request, "Product updated"))
        }
    }

    private fun saveCallback(request: ProductRequest, message: String) = object : Callback<ApiProduct> {
        override fun onResponse(call: Call<ApiProduct>, response: Response<ApiProduct>) {
            val savedProduct = response.body()

            if (!response.isSuccessful || savedProduct == null) {
                btnSave.isEnabled = true
                Toast.makeText(this@ProductFormActivity, response.errorBody()?.string() ?: "Unable to save product", Toast.LENGTH_LONG).show()
                return
            }

            if (selectedImageUri == null) {
                btnSave.isEnabled = true
                Toast.makeText(this@ProductFormActivity, message, Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            uploadMainImage(savedProduct, request, message)
        }

        override fun onFailure(call: Call<ApiProduct>, t: Throwable) {
            btnSave.isEnabled = true
            Toast.makeText(this@ProductFormActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uploadMainImage(product: ApiProduct, request: ProductRequest, message: String) {
        val uri = selectedImageUri ?: return
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
                btnSave.isEnabled = true
                Toast.makeText(this, "Unable to read image", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            btnSave.isEnabled = true
            Toast.makeText(this, "Unable to read image: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        val mediaType = MediaType.parse(mimeType)
        val requestBody = RequestBody.create(mediaType, tempFile)
        val multipart = MultipartBody.Part.createFormData("file", fileName, requestBody)

        productApi.uploadProductImage(product.id, multipart).enqueue(object : Callback<ApiProductImage> {
            override fun onResponse(call: Call<ApiProductImage>, response: Response<ApiProductImage>) {
                tempFile.delete()

                val uploadedImage = response.body()

                if (!response.isSuccessful || uploadedImage == null) {
                    btnSave.isEnabled = true
                    Toast.makeText(this@ProductFormActivity, response.errorBody()?.string() ?: "Product saved but image upload failed", Toast.LENGTH_LONG).show()
                    return
                }

                val updatedRequest = request.copy(image = uploadedImage.imageUrl)

                productApi.updateProduct(product.id, updatedRequest).enqueue(object : Callback<ApiProduct> {
                    override fun onResponse(call: Call<ApiProduct>, response: Response<ApiProduct>) {
                        btnSave.isEnabled = true

                        if (!response.isSuccessful) {
                            Toast.makeText(this@ProductFormActivity, response.errorBody()?.string() ?: "Image uploaded but main image update failed", Toast.LENGTH_LONG).show()
                            return
                        }

                        Toast.makeText(this@ProductFormActivity, "$message with image", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                    override fun onFailure(call: Call<ApiProduct>, t: Throwable) {
                        btnSave.isEnabled = true
                        Toast.makeText(this@ProductFormActivity, "Image uploaded but product update failed", Toast.LENGTH_LONG).show()
                    }
                })
            }

            override fun onFailure(call: Call<ApiProductImage>, t: Throwable) {
                tempFile.delete()
                btnSave.isEnabled = true
                Toast.makeText(this@ProductFormActivity, "Product saved but image upload failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "product_image.jpg"

        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) fileName = cursor.getString(index)
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
}