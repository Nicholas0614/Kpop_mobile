package com.example.kpop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiVariant
import com.example.kpop.network.model.VariantRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VariantFormActivity : AppCompatActivity() {

    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }

    private lateinit var etName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etQuantity: EditText
    private lateinit var etImage: EditText
    private lateinit var etSalePrice: EditText
    private lateinit var switchSale: Switch
    private lateinit var btnSave: Button

    private var productId = 0
    private var variantId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.variant_form)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            finish()
            return
        }

        productId = intent.getIntExtra("productId", 0)
        variantId = intent.getIntExtra("variantId", 0)

        if (productId == 0) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val txtTitle = findViewById<TextView>(R.id.txtFormTitle)

        etName = findViewById(R.id.etVariantName)
        etPrice = findViewById(R.id.etVariantPrice)
        etQuantity = findViewById(R.id.etVariantQuantity)
        etImage = findViewById(R.id.etVariantImage)
        etSalePrice = findViewById(R.id.etVariantSalePrice)
        switchSale = findViewById(R.id.switchVariantSale)
        btnSave = findViewById(R.id.btnSaveVariant)

        txtTitle.text = if (variantId == 0) "Add Variant" else "Edit Variant"

        btnBack.setOnClickListener { finish() }

        switchSale.setOnCheckedChangeListener { _, checked ->
            etSalePrice.isEnabled = checked
            if (!checked) etSalePrice.error = null
        }

        etSalePrice.isEnabled = false

        btnSave.setOnClickListener { saveVariant() }

        if (variantId != 0) loadVariant()
    }

    private fun loadVariant() {
        productApi.getVariants(productId).enqueue(object : Callback<List<ApiVariant>> {
            override fun onResponse(call: Call<List<ApiVariant>>, response: Response<List<ApiVariant>>) {
                val variant = response.body()?.find { it.id == variantId }

                if (!response.isSuccessful || variant == null) {
                    Toast.makeText(this@VariantFormActivity, "Unable to load variant", Toast.LENGTH_SHORT).show()
                    finish()
                    return
                }

                etName.setText(variant.name)
                etPrice.setText(variant.price.toString())
                etQuantity.setText(variant.quantity.toString())
                etImage.setText(variant.image ?: "")
                switchSale.isChecked = variant.onSale
                etSalePrice.isEnabled = variant.onSale
                etSalePrice.setText(variant.salePrice?.toString() ?: "")
            }

            override fun onFailure(call: Call<List<ApiVariant>>, t: Throwable) {
                Toast.makeText(this@VariantFormActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveVariant() {
        val name = etName.text.toString().trim()
        val price = etPrice.text.toString().toDoubleOrNull()
        val quantity = etQuantity.text.toString().toIntOrNull()
        val image = etImage.text.toString().trim()
        val onSale = switchSale.isChecked
        val salePrice = etSalePrice.text.toString().toDoubleOrNull()

        if (name.isEmpty()) {
            etName.error = "Enter variant name"
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

        if (onSale && (salePrice == null || salePrice <= 0 || salePrice >= price)) {
            etSalePrice.error = "Sale price must be lower than normal price"
            return
        }

        val request = VariantRequest(
            name = name,
            price = price,
            onSale = onSale,
            salePrice = if (onSale) salePrice else null,
            quantity = quantity,
            image = image.ifEmpty { null }
        )

        btnSave.isEnabled = false

        if (variantId == 0) {
            productApi.addVariant(productId, request).enqueue(saveCallback("Variant added"))
        } else {
            productApi.updateVariant(variantId, request).enqueue(saveCallback("Variant updated"))
        }
    }

    private fun saveCallback(message: String) = object : Callback<ApiVariant> {
        override fun onResponse(call: Call<ApiVariant>, response: Response<ApiVariant>) {
            btnSave.isEnabled = true

            if (!response.isSuccessful) {
                Toast.makeText(this@VariantFormActivity, response.errorBody()?.string() ?: "Unable to save variant", Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(this@VariantFormActivity, message, Toast.LENGTH_SHORT).show()
            finish()
        }

        override fun onFailure(call: Call<ApiVariant>, t: Throwable) {
            btnSave.isEnabled = true
            Toast.makeText(this@VariantFormActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}