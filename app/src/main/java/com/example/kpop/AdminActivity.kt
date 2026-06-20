package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminProductAdapter
import com.example.kpop.database.AppDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var adminAdapter: AdminProductAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        db = AppDatabase.getDatabase(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val btnAddProduct = findViewById<ImageButton>(R.id.btnAddProduct)
        recyclerView = findViewById(R.id.adminProductRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAddProduct.setOnClickListener {
            val intent = Intent(this, ProductFormActivity::class.java)
            startActivity(intent)
        }

        loadProducts()

        val btnAdminLogout = findViewById<ImageButton>(R.id.btnAdminLogout)

        btnAdminLogout.setOnClickListener {

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
    }

    override fun onResume() {
        super.onResume()
        if (::db.isInitialized) {
            loadProducts()
        }
    }



    private fun loadProducts() {
        val productList = db.productDao().getAllProducts().toMutableList()

        adminAdapter = AdminProductAdapter(
            productList,
            onEditClick = { product ->
                val intent = Intent(this, ProductFormActivity::class.java)
                intent.putExtra("id", product.id)
                intent.putExtra("name", product.name)
                intent.putExtra("category", product.category)
                intent.putExtra("price", product.price)
                intent.putExtra("description", product.description)
                intent.putExtra("rating", product.rating)
                intent.putExtra("image", product.image)
                startActivity(intent)
            },
            onDeleteClick = { product ->
                db.productDao().deleteProduct(product)
                loadProducts()
            }
        )

        recyclerView.adapter = adminAdapter
    }
}