package com.example.kpop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.Review

class ReviewActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        db = AppDatabase.getDatabase(this)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val imgProduct = findViewById<ImageView>(R.id.imgProduct)
        val txtProductName = findViewById<TextView>(R.id.txtProductName)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        val userId = getSharedPreferences("user_session", MODE_PRIVATE)
            .getInt("userId", 0)

        val productId = intent.getIntExtra("productId", 0)
        val productName = intent.getStringExtra("productName") ?: ""
        val image = intent.getIntExtra("image", 0)

        imgProduct.setImageResource(image)
        txtProductName.text = productName

        btnBack.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {

            val rating = ratingBar.rating
            val comment = etComment.text.toString().trim()


            if (rating == 0f) {
                Toast.makeText(this, "Please select a rating.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val existingReview = db.reviewDao().getUserReview(userId, productId)

            if (existingReview != null) {
                Toast.makeText(this, "You have already reviewed this product.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userName = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("name", "") ?: ""

            val currentDate = java.text.SimpleDateFormat(
                "dd/MM/yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            db.reviewDao().insertReview(
                Review(
                    userId = userId,
                    userName = userName,
                    productId = productId,
                    rating = rating,
                    comment = comment,
                    date = currentDate
                )
            )

            val avgRating = db.reviewDao().getAverageRating(productId) ?: 0f

            db.productDao().updateProductRating(
                productId,
                avgRating.toDouble()
            )

            Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show()

            finish()
        }
    }
}