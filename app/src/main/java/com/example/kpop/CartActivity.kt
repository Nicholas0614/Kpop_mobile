package com.example.kpop

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.CartAdapter
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.Order

class CartActivity : AppCompatActivity() {

    private lateinit var cartAdapter: CartAdapter
    private lateinit var txtTotal: TextView
    private lateinit var db: AppDatabase

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomSummary: LinearLayout
    private lateinit var emptyCartLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart)

        db = AppDatabase.getDatabase(this)

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

        val btnBack = findViewById<ImageButton>(R.id.cartBtnBack)
        recyclerView = findViewById(R.id.cartRecyclerView)
        val btnCheckout = findViewById<Button>(R.id.btnCheckout)

        txtTotal = findViewById(R.id.txtTotal)
        bottomSummary = findViewById(R.id.bottomSummary)
        emptyCartLayout = findViewById(R.id.emptyCartLayout)

        btnBack.setOnClickListener {
            finish()
        }

        val userId = getSharedPreferences("user_session", MODE_PRIVATE)
            .getInt("userId", 0)

        val cartList = db.cartDao().getCartItems(userId)

        cartAdapter = CartAdapter(cartList) {
            updateTotal()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = cartAdapter

        updateTotal()

        btnCheckout.setOnClickListener {

            val userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("userId", 0)

            if (userId == 0) {
                Toast.makeText(this, "Please login before checkout", Toast.LENGTH_SHORT).show()

                val intent = android.content.Intent(this, LoginActivity::class.java)
                startActivity(intent)

                return@setOnClickListener
            }

            if (cartList.isEmpty()) {
                checkEmptyCart()
            } else {
                val dialog = Dialog(this)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.checkout)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.setCancelable(false)

                dialog.show()

                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    val currentDate = java.text.SimpleDateFormat(
                        "dd/MM/yyyy",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date())

                    for (item in cartList) {
                        db.orderDao().insertOrder(
                            Order(
                                userId = userId,
                                productId = item.productId,
                                name = item.name,
                                category = item.category,
                                price = item.price,
                                image = item.image,
                                quantity = item.quantity,
                                totalPrice = item.price * item.quantity,
                                date = currentDate
                            )
                        )
                    }

                    db.cartDao().clearCart(userId)
                    cartList.clear()
                    cartAdapter.notifyDataSetChanged()
                    updateTotal()

                    dialog.dismiss()
                    finish()
                }, 2000)
            }
        }
    }

    private fun updateTotal() {
        txtTotal.text = "RM${cartAdapter.getTotalPrice()}"
        checkEmptyCart()
    }

    private fun checkEmptyCart() {
        if (cartAdapter.itemCount == 0) {
            recyclerView.visibility = View.GONE
            bottomSummary.visibility = View.GONE
            emptyCartLayout.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            bottomSummary.visibility = View.VISIBLE
            emptyCartLayout.visibility = View.GONE
        }
    }
}