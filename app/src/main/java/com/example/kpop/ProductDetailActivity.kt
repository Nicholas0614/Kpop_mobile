package com.example.kpop

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CartApi
import com.example.kpop.network.api.ProductApi
import com.example.kpop.network.api.ReviewApi
import com.example.kpop.network.api.WishlistApi
import com.example.kpop.network.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity:AppCompatActivity(){

    private var product:ApiProduct?=null
    private var variants:List<ApiVariant> = emptyList()
    private var variantsLoaded=false
    private var isWishlisted=false

    private lateinit var imgProduct:ImageView
    private lateinit var txtName:TextView
    private lateinit var txtCategory:TextView
    private lateinit var txtGroup:TextView
    private lateinit var txtPrice:TextView
    private lateinit var txtRating:TextView
    private lateinit var txtDesc:TextView
    private lateinit var txtReviewList:TextView
    private lateinit var imageGallery:LinearLayout
    private lateinit var btnWishlist:ImageButton

    private val productApi by lazy{RetrofitClient.create(this,ProductApi::class.java)}
    private val cartApi by lazy{RetrofitClient.create(this,CartApi::class.java)}
    private val reviewApi by lazy{RetrofitClient.create(this,ReviewApi::class.java)}
    private val wishlistApi by lazy{RetrofitClient.create(this,WishlistApi::class.java)}

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.product_item_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.productDetailMain)){v,insets->
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom)
            insets
        }

        val productId=intent.getIntExtra("id",0)

        if(productId==0){
            Toast.makeText(this,"Invalid product",Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        imgProduct=findViewById(R.id.imgProductDetail)
        txtName=findViewById(R.id.txtProductName)
        txtCategory=findViewById(R.id.txtProductCategory)
        txtGroup=findViewById(R.id.txtProductGroup)
        txtPrice=findViewById(R.id.txtProductPrice)
        txtRating=findViewById(R.id.txtProductRating)
        txtDesc=findViewById(R.id.txtProductDesc)
        txtReviewList=findViewById(R.id.txtReviewList)
        imageGallery=findViewById(R.id.productImageGallery)
        btnWishlist=findViewById(R.id.btnWishlist)

        val btnBack=findViewById<ImageButton>(R.id.btnBack)
        val btnCart=findViewById<ImageButton>(R.id.btnCart)
        val btnBuyNow=findViewById<Button>(R.id.btnBuyNow)

        btnBack.setOnClickListener{finish()}
        btnCart.setOnClickListener{startActivity(Intent(this,CartActivity::class.java))}
        btnWishlist.setOnClickListener{toggleWishlist(productId)}
        btnBuyNow.setOnClickListener{showBuyNowSheet()}

        loadProduct(productId)
        loadImages(productId)
        loadReviews(productId)
        loadWishlist(productId)
    }

    private fun showBuyNowSheet(){
        val currentProduct=product

        if(currentProduct==null){
            Toast.makeText(this,"Product is still loading",Toast.LENGTH_SHORT).show()
            return
        }

        if(!variantsLoaded){
            Toast.makeText(this,"Product options are still loading",Toast.LENGTH_SHORT).show()
            return
        }

        val dialog=BottomSheetDialog(this)
        val view=layoutInflater.inflate(R.layout.bottom_sheet_buy_now,null,false)
        val height=(resources.displayMetrics.heightPixels*0.70).toInt()
        view.layoutParams=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,height)
        dialog.setContentView(view)

        val img=view.findViewById<ImageView>(R.id.imgSheetProduct)
        val name=view.findViewById<TextView>(R.id.txtSheetName)
        val price=view.findViewById<TextView>(R.id.txtSheetPrice)
        val stock=view.findViewById<TextView>(R.id.txtSheetStock)
        val variantTitle=view.findViewById<TextView>(R.id.txtVariantTitle)
        val variantOptions=view.findViewById<ChipGroup>(R.id.variantOptions)
        val txtQty=view.findViewById<TextView>(R.id.txtSheetQuantity)
        val btnMinus=view.findViewById<ImageButton>(R.id.btnSheetMinus)
        val btnPlus=view.findViewById<ImageButton>(R.id.btnSheetPlus)
        val btnBuy=view.findViewById<Button>(R.id.btnSheetBuyNow)

        var sheetVariant:ApiVariant?=null
        var sheetQuantity=1

        name.text=currentProduct.name
        loadImage(img,currentProduct.image)

        fun updateSheet(){
            val originalPrice=sheetVariant?.price?:currentProduct.price
            val finalPrice=effectivePrice(sheetVariant)
            val currentStock=sheetVariant?.quantity?:currentProduct.quantity

            price.text=if(finalPrice<originalPrice)"RM%.2f".format(finalPrice) else "RM%.2f".format(originalPrice)
            stock.text=if(currentStock>0)"$currentStock available" else "Out of stock"

            if(currentStock>0&&sheetQuantity>currentStock)sheetQuantity=currentStock
            if(sheetQuantity<1)sheetQuantity=1
            txtQty.text=sheetQuantity.toString()
        }

        if(variants.isEmpty()){
            variantTitle.visibility=View.GONE
            variantOptions.visibility=View.GONE
        }else{
            variantTitle.visibility=View.VISIBLE
            variantOptions.visibility=View.VISIBLE
            variantOptions.removeAllViews()

            val states=arrayOf(intArrayOf(android.R.attr.state_checked),intArrayOf())
            val backgroundColors=ColorStateList(states,intArrayOf(Color.BLACK,Color.TRANSPARENT))
            val textColors=ColorStateList(states,intArrayOf(Color.WHITE,Color.BLACK))
            val strokeColors=ColorStateList.valueOf(Color.BLACK)

            variants.forEach{variant->
                val chip=Chip(this)
                chip.id=View.generateViewId()
                chip.text="${variant.name}  RM%.2f".format(effectivePrice(variant))
                chip.isCheckable=true
                chip.isCheckedIconVisible=false
                chip.textSize=14f
                chip.chipBackgroundColor=backgroundColors
                chip.setTextColor(textColors)
                chip.chipStrokeColor=strokeColors
                chip.chipStrokeWidth=1f*resources.displayMetrics.density
                chip.chipCornerRadius=10f*resources.displayMetrics.density
                chip.chipMinHeight=44f*resources.displayMetrics.density

                chip.setOnCheckedChangeListener{_,checked->
                    if(checked){
                        sheetVariant=variant
                        sheetQuantity=1
                        loadImage(img,variant.image?:currentProduct.image)
                        updateSheet()
                    }else if(variantOptions.checkedChipId==View.NO_ID){
                        sheetVariant=null
                        sheetQuantity=1
                        loadImage(img,currentProduct.image)
                        updateSheet()
                    }
                }

                variantOptions.addView(chip)
            }
        }

        btnMinus.setOnClickListener{
            if(sheetQuantity>1)sheetQuantity--
            txtQty.text=sheetQuantity.toString()
        }

        btnPlus.setOnClickListener{
            if(variants.isNotEmpty()&&sheetVariant==null){
                Toast.makeText(this,"Please select a variant",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentStock=sheetVariant?.quantity?:currentProduct.quantity

            if(currentStock<=0){
                Toast.makeText(this,"Out of stock",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(sheetQuantity<currentStock)sheetQuantity++ else Toast.makeText(this,"Maximum stock reached",Toast.LENGTH_SHORT).show()
            txtQty.text=sheetQuantity.toString()
        }

        btnBuy.setOnClickListener{
            val userId=SessionManager(this).getUserId()

            if(userId==0){
                dialog.dismiss()
                Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,LoginActivity::class.java))
                return@setOnClickListener
            }

            if(variants.isNotEmpty()&&sheetVariant==null){
                Toast.makeText(this,"Please select a variant",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentStock=sheetVariant?.quantity?:currentProduct.quantity

            if(currentStock<=0){
                Toast.makeText(this,"Out of stock",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(sheetQuantity>currentStock){
                Toast.makeText(this,"Not enough stock",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request=CartRequest(userId,currentProduct.id,sheetVariant?.id,sheetQuantity)
            btnBuy.isEnabled=false

            cartApi.addCart(request).enqueue(object:Callback<ApiCartMutation>{
                override fun onResponse(call:Call<ApiCartMutation>,response:Response<ApiCartMutation>){
                    btnBuy.isEnabled=true

                    if(!response.isSuccessful){
                        Toast.makeText(this@ProductDetailActivity,response.errorBody()?.string()?:"Unable to add to cart",Toast.LENGTH_LONG).show()
                        return
                    }

                    Toast.makeText(this@ProductDetailActivity,"Added to cart",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }

                override fun onFailure(call:Call<ApiCartMutation>,t:Throwable){
                    btnBuy.isEnabled=true
                    Toast.makeText(this@ProductDetailActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
                }
            })
        }

        updateSheet()

        dialog.setOnShowListener{
            dialog.behavior.peekHeight=height
            dialog.behavior.state=BottomSheetBehavior.STATE_EXPANDED
            dialog.behavior.skipCollapsed=true
        }

        dialog.show()
    }

    override fun onResume(){
        super.onResume()
        val productId=intent.getIntExtra("id",0)

        if(productId!=0&&::txtReviewList.isInitialized){
            loadImages(productId)
            loadReviews(productId)
            loadWishlist(productId)
        }
    }

    private fun loadProduct(productId:Int){
        productApi.getProduct(productId).enqueue(object:Callback<ApiProduct>{
            override fun onResponse(call:Call<ApiProduct>,response:Response<ApiProduct>){
                val result=response.body()

                if(!response.isSuccessful||result==null){
                    Toast.makeText(this@ProductDetailActivity,"Unable to load product",Toast.LENGTH_SHORT).show()
                    return
                }

                product=result
                loadImage(imgProduct,result.image)
                txtName.text=result.name
                txtCategory.text=result.category?.name?:"Uncategorized"
                txtGroup.text=result.group?.name?:""
                txtDesc.text=result.description?:""
                txtRating.text="⭐ %.1f".format(result.rating?:0.0)
                setPrice(result.price,effectivePrice(null))
                loadVariants(productId)
            }

            override fun onFailure(call:Call<ApiProduct>,t:Throwable){
                Toast.makeText(this@ProductDetailActivity,"Cannot connect to server",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadVariants(productId:Int){
        variantsLoaded=false

        productApi.getVariants(productId).enqueue(object:Callback<List<ApiVariant>>{
            override fun onResponse(call:Call<List<ApiVariant>>,response:Response<List<ApiVariant>>){
                variants=response.body()?:emptyList()
                variantsLoaded=true
            }

            override fun onFailure(call:Call<List<ApiVariant>>,t:Throwable){
                variants=emptyList()
                variantsLoaded=true
            }
        })
    }

    private fun loadImages(productId:Int){
        productApi.getProductImages(productId).enqueue(object:Callback<List<ApiProductImage>>{
            override fun onResponse(call:Call<List<ApiProductImage>>,response:Response<List<ApiProductImage>>){
                imageGallery.removeAllViews()

                response.body()?.forEach{image->
                    val imageView=ImageView(this@ProductDetailActivity)
                    val size=(75*resources.displayMetrics.density).toInt()
                    val margin=(5*resources.displayMetrics.density).toInt()

                    imageView.layoutParams=LinearLayout.LayoutParams(size,size).apply{setMargins(margin,0,margin,0)}
                    imageView.scaleType=ImageView.ScaleType.CENTER_INSIDE
                    loadImage(imageView,image.imageUrl)
                    imageView.setOnClickListener{loadImage(imgProduct,image.imageUrl)}
                    imageGallery.addView(imageView)
                }
            }

            override fun onFailure(call:Call<List<ApiProductImage>>,t:Throwable){}
        })
    }

    private fun loadImage(imageView:ImageView,image:String?){
        if(image.isNullOrBlank()){
            imageView.setImageResource(R.drawable.ic_launcher_background)
            return
        }

        if(!image.startsWith("http://")&&!image.startsWith("https://")&&!image.contains("/")){
            val resourceId=imageResource(image)

            if(resourceId!=R.drawable.ic_launcher_background){
                imageView.setImageResource(resourceId)
                return
            }
        }

        Glide.with(this).load(RetrofitClient.imageUrl(image)).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(imageView)
    }

    private fun loadReviews(productId:Int){
        reviewApi.getReviews(productId).enqueue(object:Callback<List<ApiReview>>{
            override fun onResponse(call:Call<List<ApiReview>>,response:Response<List<ApiReview>>){
                val reviews=response.body()?:emptyList()

                if(reviews.isEmpty()){
                    txtReviewList.text="No reviews yet."
                    txtRating.text="⭐ %.1f (0 Reviews)".format(product?.rating?:0.0)
                    return
                }

                val average=reviews.map{it.rating}.average()
                txtRating.text="⭐ %.1f (%d Reviews)".format(average,reviews.size)

                txtReviewList.text=reviews.joinToString("\n\n"){
                    val stars="⭐".repeat(it.rating.toInt().coerceIn(0,5))
                    "${it.userName?:"User"}\n${it.date}\n$stars\n${it.comment}"
                }
            }

            override fun onFailure(call:Call<List<ApiReview>>,t:Throwable){
                txtReviewList.text="Unable to load reviews."
            }
        })
    }

    private fun loadWishlist(productId:Int){
        val userId=SessionManager(this).getUserId()

        if(userId==0){
            isWishlisted=false
            updateWishlistButton()
            return
        }

        wishlistApi.getWishlist(userId).enqueue(object:Callback<List<ApiWishlistItem>>{
            override fun onResponse(call:Call<List<ApiWishlistItem>>,response:Response<List<ApiWishlistItem>>){
                isWishlisted=response.body()?.any{it.productId==productId}==true
                updateWishlistButton()
            }

            override fun onFailure(call:Call<List<ApiWishlistItem>>,t:Throwable){}
        })
    }

    private fun toggleWishlist(productId:Int){
        val userId=SessionManager(this).getUserId()

        if(userId==0){
            Toast.makeText(this,"Please login first",Toast.LENGTH_SHORT).show()
            startActivity(Intent(this,LoginActivity::class.java))
            return
        }

        val call=if(isWishlisted)wishlistApi.removeWishlist(userId,productId) else wishlistApi.addWishlist(userId,productId)

        call.enqueue(object:Callback<ResponseBody>{
            override fun onResponse(call:Call<ResponseBody>,response:Response<ResponseBody>){
                if(!response.isSuccessful){
                    Toast.makeText(this@ProductDetailActivity,response.errorBody()?.string()?:"Wishlist failed",Toast.LENGTH_SHORT).show()
                    return
                }

                isWishlisted=!isWishlisted
                updateWishlistButton()
                Toast.makeText(this@ProductDetailActivity,if(isWishlisted)"Added to wishlist" else "Removed from wishlist",Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call:Call<ResponseBody>,t:Throwable){
                Toast.makeText(this@ProductDetailActivity,"Server error",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateWishlistButton(){
        btnWishlist.setImageResource(if(isWishlisted)R.drawable.favorite_filled else R.drawable.favorite_border)
    }

    private fun effectivePrice(variant:ApiVariant?):Double{
        val currentProduct=product

        return when{
            variant?.onSale==true&&variant.salePrice!=null->variant.salePrice
            variant!=null->variant.price
            currentProduct?.onSale==true&&currentProduct.salePrice!=null->currentProduct.salePrice
            currentProduct!=null->currentProduct.price
            else->0.0
        }
    }

    private fun setPrice(originalPrice:Double,finalPrice:Double){
        if(finalPrice>=originalPrice){
            txtPrice.text="RM%.2f".format(originalPrice)
            return
        }

        val normal="RM%.2f".format(originalPrice)
        val sale="RM%.2f".format(finalPrice)
        val text=SpannableString("$normal  $sale")

        text.setSpan(StrikethroughSpan(),0,normal.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD),normal.length+2,text.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtPrice.text=text
    }

    private fun imageResource(image:String?):Int{
        if(image.isNullOrBlank())return R.drawable.ic_launcher_background

        val imageName=image.substringAfterLast("/").substringBeforeLast(".").replace("-","_").lowercase()
        val resourceId=resources.getIdentifier(imageName,"drawable",packageName)

        return if(resourceId!=0)resourceId else R.drawable.ic_launcher_background
    }
}