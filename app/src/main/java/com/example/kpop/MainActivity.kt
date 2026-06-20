package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kpop.adapter.BannerAdapter
import com.example.kpop.adapter.ProductAdapter
import com.example.kpop.database.AppDatabase
import com.example.kpop.mockData.MockDataBanner
import com.example.kpop.model.Product
import android.app.Dialog
import android.media.Image
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var bannerViewPager: ViewPager2
    private lateinit var productAdapter: ProductAdapter
    private lateinit var db: AppDatabase

    private var productList = listOf<Product>()

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var btnLoginPage: ImageButton
    private lateinit var btnLogout: ImageButton
    private lateinit var txtUserStatus: TextView

    private val bannerRunnable = object : Runnable {
        override fun run() {
            val nextItem =
                (bannerViewPager.currentItem + 1) % MockDataBanner.bannerList.size

            bannerViewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        db = AppDatabase.getDatabase(this)

        productList = db.productDao().getAllProducts()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.productRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        productAdapter = ProductAdapter(productList) { product ->
            val intent = Intent(this, ProductDetailActivity::class.java)

            intent.putExtra("id", product.id)
            intent.putExtra("name", product.name)
            intent.putExtra("category", product.category)
            intent.putExtra("price", product.price)
            intent.putExtra("description", product.description)
            intent.putExtra("rating", product.rating)
            intent.putExtra("image", product.image)

            startActivity(intent)
        }

        recyclerView.adapter = productAdapter

        val searchInput = findViewById<EditText>(R.id.searchInput)

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                val keyword = s.toString()
                    .lowercase()
                    .replace(" ", "")

                val filteredList = productList.filter { product ->
                    product.name.lowercase().replace(" ", "").contains(keyword) ||
                            product.category.lowercase().replace(" ", "").contains(keyword)
                }

                productAdapter.updateList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnLoginPage = findViewById(R.id.btnLoginPage)
        btnLogout = findViewById(R.id.btnLogout)
        txtUserStatus = findViewById(R.id.txtUserStatus)

        btnLoginPage.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnLogout.setOnClickListener {

            val dialogView = layoutInflater.inflate(R.layout.logout_dialog, null)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialogView.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener {
                dialog.dismiss()
            }

            dialogView.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener {

                getSharedPreferences("user_session", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()

                dialog.dismiss()
            }

            dialog.show()
        }

        val btnOrders = findViewById<ImageButton>(R.id.btnOrders)

        btnOrders.setOnClickListener {
            val userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("userId", 0)

            if (userId == 0) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                startActivity(Intent(this, PurchaseHistoryActivity::class.java))
            }
        }





        val btnAlbums = findViewById<Button>(R.id.btnAlbums)
        val btnPhotocards = findViewById<Button>(R.id.btnPhotocards)
        val btnLightsticks = findViewById<Button>(R.id.btnLightsticks)
        val btnMerch = findViewById<Button>(R.id.btnMerch)

        btnAlbums.setOnClickListener {
            val filteredList = productList.filter {
                it.category.equals("Album", ignoreCase = true)
            }

            showLoadingThenFilter(filteredList)
        }

        btnPhotocards.setOnClickListener {
            val filteredList = productList.filter {
                it.category.equals("Photocard", ignoreCase = true)
            }
            showLoadingThenFilter(filteredList)
        }

        btnLightsticks.setOnClickListener {
            val filteredList = productList.filter {
                it.category.equals("Lightstick", ignoreCase = true)
            }
            showLoadingThenFilter(filteredList)
        }

        btnMerch.setOnClickListener {
            val filteredList = productList.filter {
                it.category.equals("Merch", ignoreCase = true)
            }

            showLoadingThenFilter(filteredList)
        }

        bannerViewPager = findViewById(R.id.bannerViewPager)
        bannerViewPager.adapter = BannerAdapter(MockDataBanner.bannerList)
        handler.postDelayed(bannerRunnable, 3000)

        val btnCart = findViewById<ImageButton>(R.id.btnCart)

        btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        updateCartBadge()
    }

    private fun showLoadingThenFilter(filteredList: List<Product>) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.loading)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            productAdapter.updateList(filteredList)
            dialog.dismiss()
        }, 800)
    }

    private fun updateLoginUI() {
        val pref = getSharedPreferences("user_session", MODE_PRIVATE)

        val userId = pref.getInt("userId", 0)
        val name = pref.getString("name", "")
        val role = pref.getString("role", "")
        val btnOrders = findViewById<ImageButton>(R.id.btnOrders)

        if (userId == 0 || role == "admin") {

            txtUserStatus.text = ""

            btnLoginPage.visibility = View.VISIBLE
            btnLogout.visibility = View.GONE
            btnOrders.visibility = View.GONE

        } else {

            txtUserStatus.text = "Hi, $name"

            btnLoginPage.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            btnOrders.visibility = View.VISIBLE
        }
    }

    private fun updateCartBadge() {
        val badge = findViewById<TextView>(R.id.txtCartBadge)

        val userId = getSharedPreferences("user_session", MODE_PRIVATE)
            .getInt("userId", 0)

        val count = db.cartDao().getCartCount(userId)

        if (count == 0) {
            badge.visibility = View.GONE
        } else {
            badge.visibility = View.VISIBLE
            badge.text = count.toString()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::db.isInitialized) {
            productList = db.productDao().getAllProducts()
            productAdapter.updateList(productList)
            updateCartBadge()
            updateLoginUI()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(bannerRunnable)
    }
}