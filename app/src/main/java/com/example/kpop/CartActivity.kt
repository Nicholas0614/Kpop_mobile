package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.CartAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CartApi
import com.example.kpop.network.model.ApiCartItem
import com.example.kpop.network.model.CartRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartActivity:AppCompatActivity(){

    private lateinit var cartAdapter:CartAdapter
    private lateinit var txtTotal:TextView
    private lateinit var txtSelectedCount:TextView
    private lateinit var recyclerView:RecyclerView
    private lateinit var bottomSummary:LinearLayout
    private lateinit var emptyCartLayout:LinearLayout
    private lateinit var cbSelectAll:CheckBox
    private lateinit var btnCheckout:Button
    private var updatingSelectAll=false

    private val cartApi by lazy{RetrofitClient.create(this,CartApi::class.java)}

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        recyclerView=findViewById(R.id.cartRecyclerView)
        txtTotal=findViewById(R.id.txtTotal)
        txtSelectedCount=findViewById(R.id.txtSelectedCount)
        bottomSummary=findViewById(R.id.bottomSummary)
        emptyCartLayout=findViewById(R.id.emptyCartLayout)
        cbSelectAll=findViewById(R.id.cbSelectAll)
        btnCheckout=findViewById(R.id.btnCheckout)

        findViewById<ImageButton>(R.id.cartBtnBack).setOnClickListener{finish()}

        cartAdapter=CartAdapter(mutableListOf(),{item,quantity->updateQuantity(item,quantity)},{item->deleteItem(item)},{updateSelectionSummary()})

        recyclerView.layoutManager=LinearLayoutManager(this)
        recyclerView.adapter=cartAdapter

        cbSelectAll.setOnCheckedChangeListener{_,checked->
            if(!updatingSelectAll)cartAdapter.selectAll(checked)
        }

        btnCheckout.setOnClickListener{
            val userId=SessionManager(this).getUserId()

            if(userId==0){
                Toast.makeText(this,"Please login before checkout",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,LoginActivity::class.java))
                return@setOnClickListener
            }

            val selectedIds=cartAdapter.getSelectedIds()

            if(selectedIds.isEmpty()){
                Toast.makeText(this,"Please select at least one item",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent=Intent(this,CheckoutActivity::class.java)
            intent.putIntegerArrayListExtra("selectedCartIds",ArrayList(selectedIds))
            startActivity(intent)
        }
    }

    override fun onResume(){
        super.onResume()

        if(SessionManager(this).getUserId()==0){
            Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
            return
        }

        loadCart()
    }

    private fun loadCart(){
        val userId=SessionManager(this).getUserId()

        cartApi.getCart(userId).enqueue(object:Callback<List<ApiCartItem>>{
            override fun onResponse(call:Call<List<ApiCartItem>>,response:Response<List<ApiCartItem>>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CartActivity,response.errorBody()?.string()?:"Unable to load cart",Toast.LENGTH_SHORT).show()
                    return
                }

                cartAdapter.updateList(response.body()?:emptyList())
                updateSelectionSummary()
                checkEmptyCart()
            }

            override fun onFailure(call:Call<List<ApiCartItem>>,t:Throwable){
                Toast.makeText(this@CartActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateQuantity(item:ApiCartItem,quantity:Int){
        val request=CartRequest(item.userId,item.productId,item.variantId,quantity)

        cartApi.updateCart(item.id,request).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CartActivity,response.errorBody()?.string()?:"Unable to update quantity",Toast.LENGTH_SHORT).show()
                    return
                }

                loadCart()
            }

            override fun onFailure(call:Call<ResponseBody>,t:Throwable){
                Toast.makeText(this@CartActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deleteItem(item:ApiCartItem){
        cartApi.deleteCart(item.id).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CartActivity,response.errorBody()?.string()?:"Unable to remove item",Toast.LENGTH_SHORT).show()
                    return
                }

                Toast.makeText(this@CartActivity,"Removed from cart",Toast.LENGTH_SHORT).show()
                loadCart()
            }

            override fun onFailure(call:Call<ResponseBody>,t:Throwable){
                Toast.makeText(this@CartActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateSelectionSummary(){
        val count=cartAdapter.getSelectedCount()
        txtTotal.text="RM%.2f".format(cartAdapter.getSelectedTotalPrice())
        txtSelectedCount.text="$count selected"
        btnCheckout.text=if(count>0)"Checkout ($count)" else "Checkout"
        btnCheckout.isEnabled=count>0
        btnCheckout.alpha=if(count>0)1f else 0.5f

        updatingSelectAll=true
        cbSelectAll.isChecked=cartAdapter.areAllSelected()
        updatingSelectAll=false
    }

    private fun checkEmptyCart(){
        val empty=cartAdapter.itemCount==0
        recyclerView.visibility=if(empty)View.GONE else View.VISIBLE
        bottomSummary.visibility=if(empty)View.GONE else View.VISIBLE
        emptyCartLayout.visibility=if(empty)View.VISIBLE else View.GONE
    }
}