package com.example.kpop

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.GroupPageProductAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CartApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.model.ApiCartItem
import com.example.kpop.network.model.ApiProduct
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroupProductsActivity:AppCompatActivity(){

    private lateinit var productAdapter:GroupPageProductAdapter
    private lateinit var btnAll:MaterialButton
    private lateinit var btnAlbum:MaterialButton
    private lateinit var btnLightstick:MaterialButton
    private lateinit var btnMerch:MaterialButton
    private var groupName=""
    private var groupProducts:List<ApiProduct> = emptyList()
    private var selectedCategory:String?=null

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_products)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.groupProductsMain)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets
        }

        groupName=intent.getStringExtra("groupName")?:""
        if(groupName.isBlank()){finish();return}

        findViewById<TextView>(R.id.txtGroupTitle).text=groupName.uppercase()
        findViewById<TextView>(R.id.txtHeroGroupName).text=groupName.uppercase()

        val recycler=findViewById<RecyclerView>(R.id.groupPageRecyclerView)
        recycler.layoutManager=GridLayoutManager(this,2)
        recycler.isNestedScrollingEnabled=false
        recycler.addItemDecoration(GridSpacingDecoration(2,14.dp(),false))

        productAdapter=GroupPageProductAdapter(emptyList()){product->
            startActivity(Intent(this,ProductDetailActivity::class.java).putExtra("id",product.id))
        }
        recycler.adapter=productAdapter

        btnAll=findViewById(R.id.btnFilterAll);btnAlbum=findViewById(R.id.btnFilterAlbum)
        btnLightstick=findViewById(R.id.btnFilterLightstick);btnMerch=findViewById(R.id.btnFilterMerch)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}
        findViewById<ImageButton>(R.id.btnGroupCart).setOnClickListener{startActivity(Intent(this,CartActivity::class.java))}

        btnAll.setOnClickListener{selectCategory(null)}
        btnAlbum.setOnClickListener{selectCategory("Album")}
        btnLightstick.setOnClickListener{selectCategory("Lightstick")}
        btnMerch.setOnClickListener{selectCategory("Merch")}

        updateFilterButtons();loadProducts();updateCartBadge()
    }

    private fun loadProducts(){
        RetrofitClient.create(this,ProductApi::class.java).getProducts().enqueue(object:Callback<List<ApiProduct>>{
            override fun onResponse(call:Call<List<ApiProduct>>,response:Response<List<ApiProduct>>){
                if(!response.isSuccessful){Toast.makeText(this@GroupProductsActivity,"Failed to load products",Toast.LENGTH_SHORT).show();return}
                groupProducts=(response.body()?:emptyList()).filter{it.group?.name.equals(groupName,true)}
                filterProducts()
            }
            override fun onFailure(call:Call<List<ApiProduct>>,t:Throwable){Toast.makeText(this@GroupProductsActivity,"Cannot connect to server: ${t.message}",Toast.LENGTH_LONG).show()}
        })
    }

    private fun selectCategory(category:String?){selectedCategory=category;filterProducts();updateFilterButtons()}

    private fun filterProducts(){
        val filtered=if(selectedCategory==null)groupProducts else groupProducts.filter{it.category?.name.equals(selectedCategory,true)}
        productAdapter.updateList(filtered)
        findViewById<TextView>(R.id.txtNoProducts).visibility=if(filtered.isEmpty())View.VISIBLE else View.GONE
    }

    private fun updateFilterButtons(){
        listOf(btnAll,btnAlbum,btnLightstick,btnMerch).forEach{
            it.backgroundTintList=ColorStateList.valueOf(Color.WHITE);it.setTextColor(Color.BLACK)
            it.strokeColor=ColorStateList.valueOf(Color.parseColor("#DDDDDD"));it.strokeWidth=1
        }

        val selected=when(selectedCategory){"Album"->btnAlbum;"Lightstick"->btnLightstick;"Merch"->btnMerch;else->btnAll}
        selected.backgroundTintList=ColorStateList.valueOf(Color.BLACK);selected.setTextColor(Color.WHITE);selected.strokeWidth=0
    }

    private fun updateCartBadge(){
        val badge=findViewById<TextView>(R.id.txtGroupCartBadge);val userId=SessionManager(this).getUserId()
        if(userId==0){badge.visibility=View.GONE;return}

        RetrofitClient.create(this,CartApi::class.java).getCart(userId).enqueue(object:Callback<List<ApiCartItem>>{
            override fun onResponse(call:Call<List<ApiCartItem>>,response:Response<List<ApiCartItem>>){
                val count=response.body()?.sumOf{it.quantity}?:0
                badge.visibility=if(count==0)View.GONE else View.VISIBLE
                badge.text=if(count>99)"99+" else count.toString()
            }
            override fun onFailure(call:Call<List<ApiCartItem>>,t:Throwable){badge.visibility=View.GONE}
        })
    }

    private fun Int.dp():Int=(this*resources.displayMetrics.density).toInt()

    private class GridSpacingDecoration(private val spanCount:Int,private val spacing:Int,private val includeEdge:Boolean):RecyclerView.ItemDecoration(){
        override fun getItemOffsets(outRect:Rect,view:View,parent:RecyclerView,state:RecyclerView.State){
            val position=parent.getChildAdapterPosition(view)
            if(position==RecyclerView.NO_POSITION)return
            val column=position%spanCount

            if(includeEdge){
                outRect.left=spacing-column*spacing/spanCount
                outRect.right=(column+1)*spacing/spanCount
                if(position<spanCount)outRect.top=spacing
                outRect.bottom=spacing
            }else{
                outRect.left=column*spacing/spanCount
                outRect.right=spacing-(column+1)*spacing/spanCount
                if(position>=spanCount)outRect.top=spacing
            }
        }
    }

    override fun onResume(){super.onResume();updateCartBadge()}
}