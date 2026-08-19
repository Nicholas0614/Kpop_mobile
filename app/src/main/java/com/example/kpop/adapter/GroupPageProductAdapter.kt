package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kpop.R
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.model.ApiProduct

class GroupPageProductAdapter(
    private var list:List<ApiProduct>,
    private val onClick:(ApiProduct)->Unit
):RecyclerView.Adapter<GroupPageProductAdapter.ViewHolder>(){

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val image:ImageView=view.findViewById(R.id.imgProduct)
        val name:TextView=view.findViewById(R.id.txtName)
        val category:TextView=view.findViewById(R.id.txtCategory)
        val rating:TextView=view.findViewById(R.id.txtRating)
        val price:TextView=view.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder{
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.group_page_product_item,parent,false))
    }

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val product=list[position]
        holder.name.text=product.name
        holder.category.text="${product.category?.name?:""} • ${product.group?.name?:""}"
        holder.rating.text="⭐ %.1f".format(product.rating?:0.0)

        val currentPrice=if(product.onSale&&product.salePrice!=null)product.salePrice else product.price
        holder.price.text="RM%.2f".format(currentPrice)

        loadImage(holder.image,product.image)
        holder.itemView.setOnClickListener{onClick(product)}
    }

    private fun loadImage(imageView:ImageView,image:String?){
        val context=imageView.context

        if(image.isNullOrBlank()){
            imageView.setImageResource(R.drawable.ic_launcher_background)
            return
        }

        if(!image.startsWith("http://")&&!image.startsWith("https://")&&!image.contains("/")){
            val imageName=image.substringAfterLast("/").substringBeforeLast(".").replace("-","_").lowercase()
            val resourceId=context.resources.getIdentifier(imageName,"drawable",context.packageName)

            if(resourceId!=0){
                imageView.setImageResource(resourceId)
                return
            }
        }

        Glide.with(context)
            .load(RetrofitClient.imageUrl(image))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }

    override fun getItemCount():Int=list.size

    fun updateList(newList:List<ApiProduct>){
        list=newList
        notifyDataSetChanged()
    }
}