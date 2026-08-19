package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AddressAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AddressApi
import com.example.kpop.network.model.ApiAddress
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddressActivity:AppCompatActivity(){

    private val addressApi by lazy{RetrofitClient.create(this,AddressApi::class.java)}
    private lateinit var recyclerView:RecyclerView
    private lateinit var adapter:AddressAdapter

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.address)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        recyclerView=findViewById(R.id.addressRecyclerView)
        recyclerView.layoutManager=LinearLayoutManager(this)

        adapter=AddressAdapter(
            emptyList(),
            {address->
                val result=Intent()
                result.putExtra("addressId",address.id)
                setResult(RESULT_OK,result)
                finish()
            },
            {address->
                val intent=Intent(this,EditAddressActivity::class.java)
                intent.putExtra("addressId",address.id)
                startActivity(intent)
            }
        )

        recyclerView.adapter=adapter

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}

        loadAddresses()
    }

    override fun onResume(){
        super.onResume()
        if(::adapter.isInitialized)loadAddresses()
    }

    private fun loadAddresses(){
        val userId=SessionManager(this).getUserId()

        addressApi.getAddresses(userId).enqueue(object:Callback<List<ApiAddress>>{
            override fun onResponse(call:Call<List<ApiAddress>>,response:Response<List<ApiAddress>>){
                if(!response.isSuccessful){
                    Toast.makeText(this@AddressActivity,"Unable to load addresses",Toast.LENGTH_SHORT).show()
                    return
                }

                val list=response.body()?:emptyList()
                adapter.updateList(list)

                val defaultAddress=list.find{it.defaultAddress}

                if(defaultAddress!=null){
                    adapter.setSelectedAddress(defaultAddress.id)
                }
            }

            override fun onFailure(call:Call<List<ApiAddress>>,t:Throwable){
                Toast.makeText(this@AddressActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }
}