package com.example.kpop

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.Product

class ProductFormActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    private val categories = listOf("Album", "Photocard", "Lightstick", "Merch")

    private val imageNames = listOf(
        "Aespa Album",
        "IU Album",
        "IU Lightstick",
        "BABYMONSTER Lightstick",
        "NMIXX Merch",
        "IU Merch",
        "LE SSERAFIM Photocard",
        "Aespa Photocard"
    )

    private val imageMap = mapOf(
        "Aespa Album" to R.drawable.aespa,
        "IU Album" to R.drawable.banner3,
        "IU Lightstick" to R.drawable.iu_lightstick,
        "BABYMONSTER Lightstick" to R.drawable.bm_stick,
        "NMIXX Merch" to R.drawable.nmixx_merch,
        "IU Merch" to R.drawable.iu_merch,
        "LE SSERAFIM Photocard" to R.drawable.le_photocard,
        "Aespa Photocard" to R.drawable.aespa_pho
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_product)

        db = AppDatabase.getDatabase(this)

        val txtFormTitle = findViewById<TextView>(R.id.txtFormTitle)
        val etProductName = findViewById<EditText>(R.id.etProductName)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val etProductPrice = findViewById<EditText>(R.id.etProductPrice)
        val etProductDesc = findViewById<EditText>(R.id.etProductDesc)
        val spinnerImage = findViewById<Spinner>(R.id.spinnerImage)
        val btnSaveProduct = findViewById<Button>(R.id.btnSaveProduct)

        spinnerCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        spinnerImage.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            imageNames
        )

        val productId = intent.getIntExtra("id", 0)
        val isEdit = productId != 0

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        if (isEdit) {
            txtFormTitle.text = "Edit Product"

            val name = intent.getStringExtra("name") ?: ""
            val category = intent.getStringExtra("category") ?: ""
            val price = intent.getDoubleExtra("price", 0.0)
            val description = intent.getStringExtra("description") ?: ""
            val image = intent.getIntExtra("image", R.drawable.ic_launcher_background)

            etProductName.setText(name)
            etProductPrice.setText(price.toString())
            etProductDesc.setText(description)

            val categoryIndex = categories.indexOf(category)
            if (categoryIndex >= 0) {
                spinnerCategory.setSelection(categoryIndex)
            }

            val imageIndex = imageMap.values.toList().indexOf(image)
            if (imageIndex >= 0) {
                spinnerImage.setSelection(imageIndex)
            }
        }

        btnSaveProduct.setOnClickListener {
            val name = etProductName.text.toString().trim()
            val category = spinnerCategory.selectedItem.toString()
            val priceText = etProductPrice.text.toString().trim()
            val description = etProductDesc.text.toString().trim()
            val imageName = spinnerImage.selectedItem.toString()
            val image = imageMap[imageName] ?: R.drawable.ic_launcher_background

            if (name.isEmpty() || priceText.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceText.toDouble()

            if (isEdit) {
                db.productDao().updateProduct(
                    Product(
                        id = productId,
                        name = name,
                        category = category,
                        price = price,
                        description = description,
                        rating = intent.getDoubleExtra("rating", 0.0),
                        image = image
                    )
                )

                Toast.makeText(this, "Product updated", Toast.LENGTH_SHORT).show()
            } else {
                db.productDao().insertProduct(
                    Product(
                        name = name,
                        category = category,
                        price = price,
                        description = description,
                        rating = 0.0,
                        image = image
                    )
                )

                Toast.makeText(this, "Product added", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}