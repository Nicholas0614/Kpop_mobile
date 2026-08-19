package com.example.kpop

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.kpop.adapter.BannerAdapter
import com.example.kpop.adapter.ProductAdapter
import com.example.kpop.mockData.MockDataBanner
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CartApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiCartItem
import com.example.kpop.network.model.ApiProduct
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity:AppCompatActivity(){

    private lateinit var bannerViewPager:ViewPager2
    private lateinit var groupContainer:LinearLayout
    private var productList:List<ApiProduct> = emptyList()
    private val handler=Handler(Looper.getMainLooper())

    private lateinit var btnLoginPage:ImageButton
    private lateinit var btnLogout:ImageButton
    private lateinit var btnWishlistPage:Button
    private lateinit var btnCouponsPage:Button
    private lateinit var btnOrders:ImageButton
    private lateinit var btnCart:ImageButton
    private lateinit var btnTopMenu:ImageButton
    private lateinit var txtUserStatus:TextView

    private val bannerRunnable=object:Runnable{
        override fun run(){if(MockDataBanner.bannerList.isNotEmpty()){val next=(bannerViewPager.currentItem+1)%MockDataBanner.bannerList.size;bannerViewPager.setCurrentItem(next,true)};handler.postDelayed(this,3000)}
    }

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState);enableEdgeToEdge();setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)){v,insets->val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets}

        groupContainer=findViewById(R.id.groupProductContainer)

        val searchInput=findViewById<EditText>(R.id.searchInput)
        searchInput.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){}
            override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){
                val keyword=s.toString().trim().lowercase()
                val filtered=if(keyword.isBlank())productList else productList.filter{it.name.lowercase().contains(keyword)||(it.category?.name?.lowercase()?.contains(keyword)==true)||(it.group?.name?.lowercase()?.contains(keyword)==true)}
                showGroupedProducts(filtered)
            }
            override fun afterTextChanged(s:Editable?){}
        })

        btnLoginPage=findViewById(R.id.btnLoginPage);btnLogout=findViewById(R.id.btnLogout);btnWishlistPage=findViewById(R.id.btnWishlistPage);btnCouponsPage=findViewById(R.id.btnCouponsPage)
        btnOrders=findViewById(R.id.btnOrders);btnCart=findViewById(R.id.btnCart);btnTopMenu=findViewById(R.id.btnTopMenu);txtUserStatus=findViewById(R.id.txtUserStatus)

        btnLoginPage.setOnClickListener{startActivity(Intent(this,LoginActivity::class.java))}
        btnCart.setOnClickListener{startActivity(Intent(this,CartActivity::class.java))}
        btnWishlistPage.setOnClickListener{if(SessionManager(this).getUserId()==0){Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show();startActivity(Intent(this,LoginActivity::class.java))}else startActivity(Intent(this,WishlistActivity::class.java))}
        btnCouponsPage.setOnClickListener{if(SessionManager(this).getUserId()==0){Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show();startActivity(Intent(this,LoginActivity::class.java))}else startActivity(Intent(this,CouponActivity::class.java))}
        btnOrders.setOnClickListener{if(SessionManager(this).getUserId()==0){Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show();startActivity(Intent(this,LoginActivity::class.java))}else startActivity(Intent(this,PurchaseHistoryActivity::class.java))}
        btnLogout.setOnClickListener{showLogoutDialog()}
        btnTopMenu.setOnClickListener{showTopMenu(it)}

        bannerViewPager=findViewById(R.id.bannerViewPager);bannerViewPager.adapter=BannerAdapter(MockDataBanner.bannerList);handler.postDelayed(bannerRunnable,3000)

        updateLoginUI();updateCartBadge()
    }

    private fun showGroupedProducts(products:List<ApiProduct>){
        groupContainer.removeAllViews()
        val grouped=products.groupBy{it.group?.name?.takeIf{name->name.isNotBlank()}?:"Other"}

        grouped.forEach{(groupName,groupProducts)->
            val section=LayoutInflater.from(this).inflate(R.layout.group_product_section,groupContainer,false)
            section.findViewById<TextView>(R.id.txtGroupName).text=groupName.uppercase()

            section.findViewById<View>(R.id.groupHeader).setOnClickListener{
                startActivity(Intent(this,GroupProductsActivity::class.java).putExtra("groupName",groupName))
            }

            val recycler=section.findViewById<RecyclerView>(R.id.groupProductRecyclerView)
            recycler.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
            recycler.isNestedScrollingEnabled=false
            recycler.adapter=ProductAdapter(groupProducts){product->
                startActivity(Intent(this,ProductDetailActivity::class.java).putExtra("id",product.id))
            }

            groupContainer.addView(section)
        }
    }

    private fun showTopMenu(anchor:View){
        val popup=PopupMenu(this,anchor)
        val pref=getSharedPreferences("user_session",MODE_PRIVATE)
        val userId=pref.getInt("userId",0);val role=pref.getString("role","")?:"";val loggedIn=userId!=0&&!role.equals("admin",true)

        if(!loggedIn)popup.menu.add(0,1,0,"Login")
        else{popup.menu.add(0,3,0,"Wishlist");popup.menu.add(0,4,1,"Vouchers");popup.menu.add(0,5,2,"My Orders");popup.menu.add(0,6,3,"Logout")}

        popup.setOnMenuItemClickListener{item->
            when(item.itemId){
                1->{startActivity(Intent(this,LoginActivity::class.java));true}
                3->{startActivity(Intent(this,WishlistActivity::class.java));true}
                4->{startActivity(Intent(this,CouponActivity::class.java));true}
                5->{startActivity(Intent(this,PurchaseHistoryActivity::class.java));true}
                6->{showLogoutDialog();true}
                else->false
            }
        }
        popup.show()
    }

    private fun showLogoutDialog(){
        val view=layoutInflater.inflate(R.layout.logout_dialog,null);val dialog=AlertDialog.Builder(this).setView(view).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        view.findViewById<Button>(R.id.btnCancelLogout).setOnClickListener{dialog.dismiss()}
        view.findViewById<Button>(R.id.btnConfirmLogout).setOnClickListener{
            getSharedPreferences("user_session",MODE_PRIVATE).edit().clear().apply()
            val intent=Intent(this,MainActivity::class.java);intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent);dialog.dismiss();finish()
        }
        dialog.show()
    }

    private fun loadProducts(){
        RetrofitClient.create(this,ProductApi::class.java).getProducts().enqueue(object:Callback<List<ApiProduct>>{
            override fun onResponse(call:Call<List<ApiProduct>>,response:Response<List<ApiProduct>>){
                if(!response.isSuccessful){Toast.makeText(this@MainActivity,"Failed to load products.",Toast.LENGTH_SHORT).show();return}
                productList=response.body()?:emptyList();showGroupedProducts(productList)
            }
            override fun onFailure(call:Call<List<ApiProduct>>,t:Throwable){Toast.makeText(this@MainActivity,"Cannot connect to server: ${t.message}",Toast.LENGTH_LONG).show()}
        })
    }

    private fun updateLoginUI(){
        val pref=getSharedPreferences("user_session",MODE_PRIVATE);val userId=pref.getInt("userId",0);val name=pref.getString("name","")
        txtUserStatus.text=if(userId==0)"" else name
    }

    private fun updateCartBadge(){
        val badge=findViewById<TextView>(R.id.txtCartBadge);val userId=SessionManager(this).getUserId()
        if(userId==0){badge.visibility=View.GONE;return}

        RetrofitClient.create(this,CartApi::class.java).getCart(userId).enqueue(object:Callback<List<ApiCartItem>>{
            override fun onResponse(call:Call<List<ApiCartItem>>,response:Response<List<ApiCartItem>>){
                val count=response.body()?.sumOf{it.quantity}?:0;badge.visibility=if(count==0)View.GONE else View.VISIBLE;badge.text=if(count>99)"99+" else count.toString()
            }
            override fun onFailure(call:Call<List<ApiCartItem>>,t:Throwable){badge.visibility=View.GONE}
        })
    }

    override fun onResume(){super.onResume();loadProducts();updateLoginUI();updateCartBadge()}
    override fun onDestroy(){super.onDestroy();handler.removeCallbacks(bannerRunnable)}
}