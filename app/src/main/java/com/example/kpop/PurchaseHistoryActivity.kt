package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.OrderAdapter
import com.example.kpop.database.AppDatabase
import android.widget.LinearLayout

class PurchaseHistoryActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.purchase_history)

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

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val recyclerView = findViewById<RecyclerView>(R.id.orderRecyclerView)
        val emptyHistoryLayout = findViewById<LinearLayout>(R.id.emptyHistoryLayout)

        val userId = getSharedPreferences("user_session", MODE_PRIVATE)
            .getInt("userId", 0)

        val orderList = db.orderDao().getOrdersByUser(userId)

        if (orderList.isEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            emptyHistoryLayout.visibility = LinearLayout.VISIBLE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            emptyHistoryLayout.visibility = LinearLayout.GONE
        }

        orderAdapter = OrderAdapter(
            orderList,
            userId,
            db.reviewDao()
        ) { order ->
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("productId", order.productId)
            intent.putExtra("productName", order.name)
            intent.putExtra("image", order.image)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = orderAdapter

        btnBack.setOnClickListener {
            finish()
        }
    }
}