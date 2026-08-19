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
import com.bumptech.glide.Glide
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.api.ReviewApi
import com.example.kpop.network.model.ApiProduct
import com.example.kpop.network.model.ApiReview
import com.example.kpop.network.model.ReviewRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewActivity : AppCompatActivity() {

    private val reviewApi by lazy { RetrofitClient.create(this, ReviewApi::class.java) }

    private val productApi by lazy{RetrofitClient.create(this, ProductApi::class.java)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.review)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val imgProduct = findViewById<ImageView>(R.id.imgProduct)
        val txtProductName = findViewById<TextView>(R.id.txtProductName)
        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val etComment = findViewById<EditText>(R.id.etComment)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        val userId = SessionManager(this).getUserId()
        val productId = intent.getIntExtra("productId", 0)
        val productName = intent.getStringExtra("productName") ?: ""

        imgProduct.setImageResource(R.drawable.ic_launcher_background)

        txtProductName.text = productName

        productApi.getProduct(productId).enqueue(object:Callback<ApiProduct>{
            override fun onResponse(call:Call<ApiProduct>,response:Response<ApiProduct>){
                val product=response.body()?:return
                Glide.with(this@ReviewActivity).load(RetrofitClient.imageUrl(product.image)).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(imgProduct)
            }

            override fun onFailure(call:Call<ApiProduct>,t:Throwable){}
        })

        btnBack.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val rating = ratingBar.rating.toDouble()
            val comment = etComment.text.toString().trim()

            if (userId == 0) {
                Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (rating == 0.0) {
                Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false

            reviewApi.getReviews(productId).enqueue(object : Callback<List<ApiReview>> {
                override fun onResponse(call: Call<List<ApiReview>>, response: Response<List<ApiReview>>) {
                    val alreadyReviewed = response.body()?.any { it.userId == userId } == true

                    if (alreadyReviewed) {
                        btnSubmit.isEnabled = true
                        Toast.makeText(this@ReviewActivity, "You already reviewed this product", Toast.LENGTH_SHORT).show()
                        return
                    }

                    submitReview(userId, productId, rating, comment, btnSubmit)
                }

                override fun onFailure(call: Call<List<ApiReview>>, t: Throwable) {
                    btnSubmit.isEnabled = true
                    Toast.makeText(this@ReviewActivity, "Unable to check reviews", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun submitReview(userId: Int, productId: Int, rating: Double, comment: String, button: Button) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val request = ReviewRequest(userId, productId, rating, comment, date)

        reviewApi.addReview(request).enqueue(object : Callback<ApiReview> {
            override fun onResponse(call: Call<ApiReview>, response: Response<ApiReview>) {
                button.isEnabled = true

                if (!response.isSuccessful) {
                    Toast.makeText(this@ReviewActivity, response.errorBody()?.string() ?: "Review failed", Toast.LENGTH_SHORT).show()
                    return
                }

                Toast.makeText(this@ReviewActivity, "Review submitted successfully", Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFailure(call: Call<ApiReview>, t: Throwable) {
                button.isEnabled = true
                Toast.makeText(this@ReviewActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}