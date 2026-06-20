package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.CartEntity
import com.example.kpop.model.Product

class ProductDetailActivity : AppCompatActivity() {

    private var quantity = 1
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_item_detail)

        db = AppDatabase.getDatabase(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.productDetailMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val imgProduct = findViewById<ImageView>(R.id.imgProductDetail)
        val txtName = findViewById<TextView>(R.id.txtProductName)
        val txtCategory = findViewById<TextView>(R.id.txtProductCategory)
        val txtPrice = findViewById<TextView>(R.id.txtProductPrice)
        val txtRating = findViewById<TextView>(R.id.txtProductRating)
        val txtDesc = findViewById<TextView>(R.id.txtProductDesc)
        val txtQuantity = findViewById<TextView>(R.id.txtQuantity)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnMinus = findViewById<ImageButton>(R.id.btnMinus)
        val btnPlus = findViewById<ImageButton>(R.id.btnPlus)
        val btnAddCart = findViewById<LottieAnimationView>(R.id.btnAddCart)
        val btnCart = findViewById<ImageButton>(R.id.btnCart)
        val txtReviewList = findViewById<TextView>(R.id.txtReviewList)

        val product = Product(
            id = intent.getIntExtra("id", 0),
            name = intent.getStringExtra("name") ?: "",
            category = intent.getStringExtra("category") ?: "",
            price = intent.getDoubleExtra("price", 0.0),
            description = intent.getStringExtra("description") ?: "",
            rating = intent.getDoubleExtra("rating", 0.0),
            image = intent.getIntExtra("image", 0)
        )

        imgProduct.setImageResource(product.image)
        txtName.text = product.name
        txtCategory.text = product.category
        txtPrice.text = "RM${product.price}"
        txtRating.text = "⭐ ${product.rating}"
        txtDesc.text = product.description
        txtQuantity.text = quantity.toString()

        btnBack.setOnClickListener {
            finish()
        }

        btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        btnPlus.setOnClickListener {
            quantity++
            txtQuantity.text = quantity.toString()
        }

        btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity--
                txtQuantity.text = quantity.toString()
            }
        }

        val avg = db.reviewDao().getAverageRating(product.id) ?: 0f
        val count = db.reviewDao().getReviewCount(product.id)

        txtRating.text = "⭐ %.1f (%d Reviews)".format(avg, count)

        val reviews = db.reviewDao().getReviewsByProduct(product.id)

        if (reviews.isEmpty()) {
            txtReviewList.text = "No reviews yet."
        } else {
            val reviewText = reviews.joinToString("\n\n") { review ->
                val stars = "⭐".repeat(review.rating.toInt())

                "${review.userName}\n${review.date}\n$stars\n${review.comment}"
            }

            txtReviewList.text = reviewText
        }

        btnAddCart.setOnClickListener {
            btnAddCart.playAnimation()

            val userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getInt("userId", 0)

            val existingItem = db.cartDao().findCartItem(userId, product.id)

            if (existingItem != null) {
                existingItem.quantity += quantity
                db.cartDao().updateCartItem(existingItem)
            } else {
                db.cartDao().insertCartItem(
                    CartEntity(
                        userId = userId,
                        productId = product.id,
                        name = product.name,
                        category = product.category,
                        price = product.price,
                        rating = product.rating,
                        image = product.image,
                        quantity = quantity
                    )
                )
            }

            btnAddCart.postDelayed({
                finish()
            }, 1800)
        }
    }
}