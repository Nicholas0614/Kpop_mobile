package com.example.kpop

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.ProductAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.api.WishlistApi
import com.example.kpop.network.model.ApiProduct
import com.example.kpop.network.model.ApiWishlistItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishlistActivity : AppCompatActivity() {

    private val wishlistApi by lazy { RetrofitClient.create(this, WishlistApi::class.java) }
    private val productApi by lazy { RetrofitClient.create(this, ProductApi::class.java) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var txtEmpty: TextView
    private lateinit var adapter: ProductAdapter

    private val products = mutableListOf<ApiProduct>()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.wishlistRecyclerView)
        txtEmpty = findViewById(R.id.txtEmptyWishlist)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapter = ProductAdapter(products) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("id", product.id)
            startActivity(intent)
        }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadWishlist()
    }

    private fun loadWishlist() {
        val userId = SessionManager(this).getUserId()

        if (userId == 0) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        wishlistApi.getWishlist(userId).enqueue(object : Callback<List<ApiWishlistItem>> {
            override fun onResponse(call: Call<List<ApiWishlistItem>>, response: Response<List<ApiWishlistItem>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@WishlistActivity, "Unable to load wishlist", Toast.LENGTH_SHORT).show()
                    return
                }

                val wishlist = response.body() ?: emptyList()

                products.clear()
                adapter.updateList(emptyList())

                if (wishlist.isEmpty()) {
                    showEmpty(true)
                    return
                }

                showEmpty(false)

                wishlist.forEach { item ->
                    loadProduct(item.productId)
                }
            }

            override fun onFailure(call: Call<List<ApiWishlistItem>>, t: Throwable) {
                Toast.makeText(this@WishlistActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadProduct(productId: Int) {
        productApi.getProduct(productId).enqueue(object : Callback<ApiProduct> {
            override fun onResponse(call: Call<ApiProduct>, response: Response<ApiProduct>) {
                val product = response.body()

                if (response.isSuccessful && product != null) {
                    products.add(product)
                    adapter.updateList(products.toList())
                    showEmpty(products.isEmpty())
                }
            }

            override fun onFailure(call: Call<ApiProduct>, t: Throwable) {}
        })
    }

    private fun showEmpty(empty: Boolean) {
        txtEmpty.visibility = if (empty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
    }
}