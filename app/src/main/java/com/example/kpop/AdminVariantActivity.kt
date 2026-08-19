package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminVariantAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AdminApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiVariant
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminVariantsActivity : AppCompatActivity() {

    private lateinit var adapter: AdminVariantAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var txtNoVariants: TextView

    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }
    private val adminApi by lazy { RetrofitClient.create(this, AdminApi::class.java) }

    private var productId = 0
    private var productName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_variants)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            finish()
            return
        }

        productId = intent.getIntExtra("productId", 0)
        productName = intent.getStringExtra("productName") ?: "Product Variants"

        if (productId == 0) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnAdd = findViewById<Button>(R.id.btnAddVariant)
        val txtProductName = findViewById<TextView>(R.id.txtProductName)

        recyclerView = findViewById(R.id.variantRecyclerView)
        txtNoVariants = findViewById(R.id.txtNoVariants)

        txtProductName.text = productName
        btnBack.setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            val intent = Intent(this, VariantFormActivity::class.java)
            intent.putExtra("productId", productId)
            startActivity(intent)
        }

        adapter = AdminVariantAdapter(
            emptyList(),
            { variant, onSale, price -> updateVariantSale(variant, onSale, price) },
            { variant ->
                val intent = Intent(this, VariantFormActivity::class.java)
                intent.putExtra("productId", productId)
                intent.putExtra("variantId", variant.id)
                startActivity(intent)
            },
            { variant -> confirmDelete(variant) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadVariants()
    }

    private fun loadVariants() {
        productApi.getVariants(productId).enqueue(object : Callback<List<ApiVariant>> {
            override fun onResponse(call: Call<List<ApiVariant>>, response: Response<List<ApiVariant>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminVariantsActivity, response.errorBody()?.string() ?: "Unable to load variants", Toast.LENGTH_SHORT).show()
                    return
                }

                val variants = response.body() ?: emptyList()

                adapter.updateList(variants)
                recyclerView.visibility = if (variants.isEmpty()) View.GONE else View.VISIBLE
                txtNoVariants.visibility = if (variants.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onFailure(call: Call<List<ApiVariant>>, t: Throwable) {
                Toast.makeText(this@AdminVariantsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateVariantSale(variant: ApiVariant, onSale: Boolean, price: Double?) {
        adminApi.updateVariantSale(variant.id, onSale, price).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminVariantsActivity, response.errorBody()?.string() ?: "Unable to update variant sale", Toast.LENGTH_SHORT).show()
                    return
                }

                Toast.makeText(this@AdminVariantsActivity, "Variant sale updated", Toast.LENGTH_SHORT).show()
                loadVariants()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminVariantsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmDelete(variant: ApiVariant) {
        AlertDialog.Builder(this)
            .setTitle("Delete Variant")
            .setMessage("Delete ${variant.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteVariant(variant.id) }
            .show()
    }

    private fun deleteVariant(variantId: Int) {
        productApi.deleteVariant(variantId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminVariantsActivity, response.errorBody()?.string() ?: "Unable to delete variant", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminVariantsActivity, "Variant deleted", Toast.LENGTH_SHORT).show()
                loadVariants()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminVariantsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}