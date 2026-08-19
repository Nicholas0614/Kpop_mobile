package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.OrderAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.OrderApi
import com.example.kpop.network.model.ApiOrder
import com.example.kpop.network.model.ApiOrderItem
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PurchaseHistoryActivity:AppCompatActivity(){

    private lateinit var orderAdapter:OrderAdapter
    private lateinit var recyclerView:RecyclerView
    private lateinit var emptyHistoryLayout:LinearLayout
    private val orderApi by lazy{RetrofitClient.create(this,OrderApi::class.java)}

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.purchase_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}

        recyclerView=findViewById(R.id.orderRecyclerView)
        emptyHistoryLayout=findViewById(R.id.emptyHistoryLayout)

        orderAdapter=OrderAdapter(emptyList(),{confirmReceived(it)},{openReview(it)})
        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter=orderAdapter
    }

    override fun onResume(){
        super.onResume()

        if(SessionManager(this).getUserId()==0){
            Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
            return
        }

        loadOrders()
    }

    private fun loadOrders(){
        val userId=SessionManager(this).getUserId()

        orderApi.getOrders(userId).enqueue(object:Callback<List<ApiOrder>>{
            override fun onResponse(call:Call<List<ApiOrder>>,response:Response<List<ApiOrder>>){
                if(!response.isSuccessful){
                    Toast.makeText(this@PurchaseHistoryActivity,response.errorBody()?.string()?:"Unable to load orders",Toast.LENGTH_SHORT).show()
                    return
                }

                val orders=(response.body()?:emptyList()).sortedByDescending{it.id}
                orderAdapter.updateList(orders)
                recyclerView.visibility=if(orders.isEmpty())View.GONE else View.VISIBLE
                emptyHistoryLayout.visibility=if(orders.isEmpty())View.VISIBLE else View.GONE
            }

            override fun onFailure(call:Call<List<ApiOrder>>,t:Throwable){
                Toast.makeText(this@PurchaseHistoryActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmReceived(order:ApiOrder){
        AlertDialog.Builder(this)
            .setTitle("Confirm Order Received")
            .setMessage("Have you received all products in Order #${order.id}?")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Yes, Received"){_,_->markReceived(order.id)}
            .show()
    }

    private fun markReceived(orderId:Int){
        orderApi.confirmReceived(orderId).enqueue(object:Callback<ApiOrder>{
            override fun onResponse(call:Call<ApiOrder>,response:Response<ApiOrder>){
                if(!response.isSuccessful){
                    Toast.makeText(this@PurchaseHistoryActivity,response.errorBody()?.string()?:"Unable to confirm order",Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@PurchaseHistoryActivity,"Order received. You can now review your products.",Toast.LENGTH_LONG).show()
                loadOrders()
            }

            override fun onFailure(call:Call<ApiOrder>,t:Throwable){
                Toast.makeText(this@PurchaseHistoryActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun openReview(item:ApiOrderItem){
        val intent=Intent(this,ReviewActivity::class.java)
        intent.putExtra("productId",item.productId)
        intent.putExtra("productName",item.productName)
        startActivity(intent)
    }
}