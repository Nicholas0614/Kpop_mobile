package com.example.kpop

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminOrderAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AdminApi
import com.example.kpop.network.model.ApiOrder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminOrdersActivity : AppCompatActivity() {

    private lateinit var adapter: AdminOrderAdapter
    private val adminApi by lazy { RetrofitClient.create(this, AdminApi::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_orders)

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            finish()
            return
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val recyclerView = findViewById<RecyclerView>(R.id.adminOrderRecyclerView)

        btnBack.setOnClickListener { finish() }

        adapter = AdminOrderAdapter(emptyList(), { order, status -> updateStatus(order, status) }, { order, tracking -> updateTracking(order, tracking) })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        adminApi.getOrders().enqueue(object : Callback<List<ApiOrder>> {
            override fun onResponse(call: Call<List<ApiOrder>>, response: Response<List<ApiOrder>>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminOrdersActivity, response.errorBody()?.string() ?: "Unable to load orders", Toast.LENGTH_SHORT).show()
                    return
                }

                adapter.updateList((response.body() ?: emptyList()).sortedByDescending { it.id })
            }

            override fun onFailure(call: Call<List<ApiOrder>>, t: Throwable) {
                Toast.makeText(this@AdminOrdersActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateStatus(order: ApiOrder, status: String) {
        adminApi.updateOrderStatus(order.id, status).enqueue(actionCallback("Order status updated"))
    }

    private fun updateTracking(order: ApiOrder, tracking: String) {
        if (tracking.isEmpty()) {
            Toast.makeText(this, "Enter a tracking number", Toast.LENGTH_SHORT).show()
            return
        }

        adminApi.updateTracking(order.id, tracking).enqueue(actionCallback("Tracking number updated"))
    }

    private fun actionCallback(message: String) = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            if (!response.isSuccessful) {
                Toast.makeText(this@AdminOrdersActivity, response.errorBody()?.string() ?: "Update failed", Toast.LENGTH_SHORT).show()
                return
            }

            Toast.makeText(this@AdminOrdersActivity, message, Toast.LENGTH_SHORT).show()
            loadOrders()
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            Toast.makeText(this@AdminOrdersActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
        }
    }
}