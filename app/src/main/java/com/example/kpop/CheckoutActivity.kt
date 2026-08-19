package com.example.kpop

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.CheckoutAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AddressApi
import com.example.kpop.network.api.CartApi
import com.example.kpop.network.api.OrderApi
import com.example.kpop.network.api.PaymentApi
import com.example.kpop.network.model.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutActivity:AppCompatActivity(){

    private val addressApi by lazy{RetrofitClient.create(this,AddressApi::class.java)}
    private val cartApi by lazy{RetrofitClient.create(this,CartApi::class.java)}
    private val orderApi by lazy{RetrofitClient.create(this,OrderApi::class.java)}
    private val paymentApi by lazy{RetrofitClient.create(this,PaymentApi::class.java)}

    private lateinit var txtAddress:TextView
    private lateinit var txtTotal:TextView
    private lateinit var txtSelectedCoupon:TextView
    private lateinit var btnPay:Button
    private lateinit var checkoutRecyclerView:RecyclerView
    private lateinit var checkoutAdapter:CheckoutAdapter

    private var selectedAddress:ApiAddress?=null
    private var selectedCoupon:UserCoupon?=null
    private var cartTotal=0.0
    private var paypalOrderId:String?=null
    private var captureInProgress=false
    private var selectedCartIds=arrayListOf<Int>()

    private val addressLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode==RESULT_OK){
            val addressId=result.data?.getIntExtra("addressId",0)?:0
            if(addressId!=0)loadSelectedAddress(addressId)
        }
    }

    private val couponLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if(result.resultCode!=RESULT_OK)return@registerForActivityResult

        val data=result.data?:return@registerForActivityResult
        val couponId=data.getIntExtra("couponId",0)
        val code=data.getStringExtra("couponCode")?:return@registerForActivityResult
        val discount=data.getDoubleExtra("discountPercentage",0.0)
        val minimum=data.getDoubleExtra("minimumPurchase",0.0)
        val expiry=data.getStringExtra("expiryDate")?:""

        if(cartTotal<minimum){
            Toast.makeText(this,"Minimum spend for $code is RM%.2f".format(minimum),Toast.LENGTH_LONG).show()
            return@registerForActivityResult
        }

        selectedCoupon=UserCoupon(couponId,code,discount,minimum,expiry,true)
        txtSelectedCoupon.text="$code  ${formatNumber(discount)}% OFF"
        updateCouponTotal()
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.checkout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.checkout)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        selectedCartIds=intent.getIntegerArrayListExtra("selectedCartIds")?:arrayListOf()

        if(selectedCartIds.isEmpty()){
            Toast.makeText(this,"No cart items selected",Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val btnBack=findViewById<ImageButton>(R.id.btnBack)
        val addressSection=findViewById<LinearLayout>(R.id.addressSection)
        val txtChangeAddress=findViewById<TextView>(R.id.txtChangeAddress)
        val couponSection=findViewById<LinearLayout>(R.id.couponSection)

        txtAddress=findViewById(R.id.txtAddress)
        txtTotal=findViewById(R.id.txtTotal)
        txtSelectedCoupon=findViewById(R.id.txtSelectedCoupon)
        btnPay=findViewById(R.id.btnPay)
        checkoutRecyclerView=findViewById(R.id.checkoutRecyclerView)

        checkoutAdapter=CheckoutAdapter(emptyList())
        checkoutRecyclerView.layoutManager=LinearLayoutManager(this)
        checkoutRecyclerView.adapter=checkoutAdapter
        checkoutRecyclerView.isNestedScrollingEnabled=false

        btnBack.setOnClickListener{finish()}

        addressSection.setOnClickListener{
            addressLauncher.launch(Intent(this,AddressActivity::class.java))
        }

        txtChangeAddress.setOnClickListener{
            addressLauncher.launch(Intent(this,AddressActivity::class.java))
        }

        couponSection.setOnClickListener{
            openCouponSelector()
        }

        txtSelectedCoupon.setOnClickListener{
            openCouponSelector()
        }

        btnPay.setOnClickListener{
            val address=selectedAddress

            if(address==null){
                Toast.makeText(this,"Please select a shipping address",Toast.LENGTH_SHORT).show()
                addressLauncher.launch(Intent(this,AddressActivity::class.java))
                return@setOnClickListener
            }

            checkout(address.id,selectedCoupon?.code)
        }

        loadDefaultAddress()
        loadCart()
        handlePayPalReturn(intent)
    }

    private fun openCouponSelector(){
        val intent=Intent(this,CouponActivity::class.java)
        intent.putExtra("selectMode",true)
        couponLauncher.launch(intent)
    }

    override fun onNewIntent(intent:Intent){
        super.onNewIntent(intent)
        setIntent(intent)
        handlePayPalReturn(intent)
    }

    override fun onResume(){
        super.onResume()
        handlePayPalReturn(intent)
    }

    private fun loadDefaultAddress(){
        val userId=SessionManager(this).getUserId()

        addressApi.getAddresses(userId).enqueue(object:Callback<List<ApiAddress>>{
            override fun onResponse(call:Call<List<ApiAddress>>,response:Response<List<ApiAddress>>){
                if(!response.isSuccessful){
                    txtAddress.text="Unable to load shipping address"
                    return
                }

                val addresses=response.body()?:emptyList()

                if(addresses.isEmpty()){
                    selectedAddress=null
                    txtAddress.text="No shipping address\nTap here to add one"
                    return
                }

                selectedAddress=addresses.find{it.defaultAddress}?:addresses.first()
                showAddress(selectedAddress!!)
            }

            override fun onFailure(call:Call<List<ApiAddress>>,t:Throwable){
                txtAddress.text="Unable to load shipping address"
            }
        })
    }

    private fun loadSelectedAddress(addressId:Int){
        addressApi.getAddress(addressId).enqueue(object:Callback<ApiAddress>{
            override fun onResponse(call:Call<ApiAddress>,response:Response<ApiAddress>){
                val address=response.body()

                if(!response.isSuccessful||address==null){
                    Toast.makeText(this@CheckoutActivity,"Unable to load address",Toast.LENGTH_SHORT).show()
                    return
                }

                selectedAddress=address
                showAddress(address)
            }

            override fun onFailure(call:Call<ApiAddress>,t:Throwable){
                Toast.makeText(this@CheckoutActivity,"Unable to load address",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddress(address:ApiAddress){
        val line2=if(address.addressLine2.isNullOrBlank())"" else "\n${address.addressLine2}"
        val defaultText=if(address.defaultAddress)"\nDefault Address" else ""

        txtAddress.text="${address.label}\n${address.recipientName}  |  ${address.phone}\n${address.addressLine1}$line2\n${address.postcode} ${address.city}, ${address.state}\n${address.country}$defaultText"
    }

    private fun loadCart(){
        val userId=SessionManager(this).getUserId()

        cartApi.getCart(userId).enqueue(object:Callback<List<ApiCartItem>>{
            override fun onResponse(call:Call<List<ApiCartItem>>,response:Response<List<ApiCartItem>>){
                if(!response.isSuccessful){
                    Toast.makeText(this@CheckoutActivity,"Unable to load cart",Toast.LENGTH_SHORT).show()
                    return
                }

                val selectedSet=selectedCartIds.toSet()
                val items=(response.body()?:emptyList()).filter{selectedSet.contains(it.id)}

                if(items.isEmpty()){
                    checkoutAdapter.updateList(emptyList())
                    txtTotal.text="Total: RM0.00"
                    btnPay.isEnabled=false
                    return
                }

                checkoutAdapter.updateList(items)
                cartTotal=items.sumOf{it.price*it.quantity}
                updateCouponTotal()
                btnPay.isEnabled=true
            }

            override fun onFailure(call:Call<List<ApiCartItem>>,t:Throwable){
                Toast.makeText(this@CheckoutActivity,"Unable to load cart: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun updateCouponTotal(){
        val coupon=selectedCoupon

        if(coupon==null){
            txtTotal.text="Subtotal: RM%.2f\nTotal: RM%.2f".format(cartTotal,cartTotal)
            return
        }

        if(cartTotal<coupon.minimumPurchase){
            selectedCoupon=null
            txtSelectedCoupon.text="›"
            txtTotal.text="Subtotal: RM%.2f\nTotal: RM%.2f".format(cartTotal,cartTotal)
            return
        }

        val discount=cartTotal*(coupon.discountPercentage/100.0)
        val finalTotal=(cartTotal-discount).coerceAtLeast(0.0)

        txtTotal.text="Subtotal: RM%.2f\nVoucher (${coupon.code}): -RM%.2f\nTotal: RM%.2f".format(
            cartTotal,
            discount,
            finalTotal
        )
    }

    private fun formatNumber(value:Double):String{
        return if(value%1.0==0.0)value.toInt().toString() else "%.2f".format(value)
    }

    private fun checkout(addressId:Int,coupon:String?){
        val userId=SessionManager(this).getUserId()
        btnPay.isEnabled=false

        orderApi.checkout(userId,addressId,coupon,selectedCartIds).enqueue(object:Callback<CheckoutResponse>{
            override fun onResponse(call:Call<CheckoutResponse>,response:Response<CheckoutResponse>){
                btnPay.isEnabled=true

                if(!response.isSuccessful){
                    Toast.makeText(this@CheckoutActivity,response.errorBody()?.string()?:"Checkout failed",Toast.LENGTH_LONG).show()
                    return
                }

                val result=response.body()

                if(result==null){
                    Toast.makeText(this@CheckoutActivity,"Checkout failed",Toast.LENGTH_SHORT).show()
                    return
                }

                paypalOrderId=result.order.paypalOrderId

                getSharedPreferences("payment_session",MODE_PRIVATE)
                    .edit()
                    .putString("paypalOrderId",paypalOrderId)
                    .apply()

                if(result.order.discountAmount>0){
                    txtTotal.text="Subtotal: RM%.2f\nVoucher: -RM%.2f\nTotal: RM%.2f".format(
                        result.order.totalPrice,
                        result.order.discountAmount,
                        result.order.finalPrice
                    )
                }else{
                    txtTotal.text="Subtotal: RM%.2f\nTotal: RM%.2f".format(
                        result.order.totalPrice,
                        result.order.finalPrice
                    )
                }

                startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(result.paypalApprovalUrl)))
            }

            override fun onFailure(call:Call<CheckoutResponse>,t:Throwable){
                btnPay.isEnabled=true
                Toast.makeText(this@CheckoutActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handlePayPalReturn(intent:Intent?){
        val uri=intent?.data?:return

        when(uri.host){
            "payment-success"->{
                if(captureInProgress)return

                val paypalId=getSharedPreferences("payment_session",MODE_PRIVATE)
                    .getString("paypalOrderId",null)

                if(paypalId.isNullOrBlank()){
                    Toast.makeText(this,"PayPal order not found",Toast.LENGTH_SHORT).show()
                    return
                }

                intent.data=null
                capturePayment(paypalId)
            }

            "payment-cancel"->{
                intent.data=null
                Toast.makeText(this,"Payment cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun capturePayment(paypalId:String){
        if(captureInProgress)return
        captureInProgress=true

        paymentApi.capturePayment(paypalId).enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                captureInProgress=false

                if(!response.isSuccessful){
                    Toast.makeText(this@CheckoutActivity,response.errorBody()?.string()?:"Payment capture failed",Toast.LENGTH_LONG).show()
                    return
                }

                getSharedPreferences("payment_session",MODE_PRIVATE).edit().clear().apply()

                Toast.makeText(this@CheckoutActivity,"Payment successful!",Toast.LENGTH_SHORT).show()

                val intent=Intent(this@CheckoutActivity,PurchaseHistoryActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }

            override fun onFailure(call:Call<ResponseBody>,t:Throwable){
                captureInProgress=false
                Toast.makeText(this@CheckoutActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }
}