package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminProductAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AdminApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiProduct
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.jvm.java

class AdminProductsActivity : AppCompatActivity() {

    private lateinit var adapter: AdminProductAdapter

    private val adminApi by lazy { RetrofitClient.create(this, AdminApi::class.java) }
    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_products)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            Toast.makeText(this, "Admin access only", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnAdd = findViewById<Button>(R.id.btnAddProduct)
        val recyclerView = findViewById<RecyclerView>(R.id.adminProductRecyclerView)

        btnBack.setOnClickListener { finish() }

        btnAdd.setOnClickListener {
            startActivity(Intent(this, ProductFormActivity::class.java))
        }

        adapter = AdminProductAdapter(
            emptyList(),

            { product, onSale, price ->
                updateSale(product, onSale, price)
            },

            { product ->
                val intent = Intent(this, AdminVariantsActivity::class.java)
                intent.putExtra("productId", product.id)
                intent.putExtra("productName", product.name)
                startActivity(intent)
            },

            { product ->
                val intent = Intent(this, AdminProductImagesActivity::class.java)
                intent.putExtra("productId", product.id)
                intent.putExtra("productName", product.name)
                startActivity(intent)
            },

            { product ->
                val intent = Intent(this, ProductFormActivity::class.java)
                intent.putExtra("productId", product.id)
                startActivity(intent)
            },

            { product ->
                confirmDelete(product)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun loadProducts() {
        adminApi.getProducts().enqueue(object : Callback<List<ApiProduct>> {
            override fun onResponse(call: Call<List<ApiProduct>>, response: Response<List<ApiProduct>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminProductsActivity, response.errorBody()?.string() ?: "Unable to load products", Toast.LENGTH_SHORT).show()
                    return
                }

                adapter.updateList(response.body() ?: emptyList())
            }

            override fun onFailure(call: Call<List<ApiProduct>>, t: Throwable) {
                Toast.makeText(this@AdminProductsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateSale(product: ApiProduct, onSale: Boolean, price: Double?) {
        adminApi.updateProductSale(product.id, onSale, price).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminProductsActivity, response.errorBody()?.string() ?: "Unable to update sale", Toast.LENGTH_SHORT).show()
                    return
                }

                Toast.makeText(this@AdminProductsActivity, "Sale updated", Toast.LENGTH_SHORT).show()
                loadProducts()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminProductsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmDelete(product: ApiProduct) {
        AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Delete ${product.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteProduct(product.id) }
            .show()
    }

    private fun deleteProduct(productId: Int) {
        productApi.deleteProduct(productId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminProductsActivity, response.errorBody()?.string() ?: "Unable to delete product", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminProductsActivity, "Product deleted", Toast.LENGTH_SHORT).show()
                loadProducts()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminProductsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}