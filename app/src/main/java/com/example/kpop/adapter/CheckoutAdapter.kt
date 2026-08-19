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
import com.example.kpop.network.model.ApiCartItem

class CheckoutAdapter(
    private var items:List<ApiCartItem>
):RecyclerView.Adapter<CheckoutAdapter.ViewHolder>(){

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val image:ImageView=view.findViewById(R.id.imgCheckoutProduct)
        val name:TextView=view.findViewById(R.id.txtCheckoutName)
        val variant:TextView=view.findViewById(R.id.txtCheckoutVariant)
        val price:TextView=view.findViewById(R.id.txtCheckoutPrice)
        val quantity:TextView=view.findViewById(R.id.txtCheckoutQuantity)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder{
        val view=LayoutInflater.from(parent.context).inflate(R.layout.checkout_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val item=items[position]

        holder.name.text=item.name
        holder.price.text="RM%.2f".format(item.price)
        holder.quantity.text="x${item.quantity}"

        if(item.variantName.isNullOrBlank()){
            holder.variant.visibility=View.GONE
        }else{
            holder.variant.visibility=View.VISIBLE
            holder.variant.text=item.variantName
        }

        loadImage(holder.image,item.image)
    }

    override fun getItemCount():Int=items.size

    fun updateList(newItems:List<ApiCartItem>){
        items=newItems
        notifyDataSetChanged()
    }

    private fun loadImage(imageView:ImageView,image:String?){
        val context=imageView.context

        if(image.isNullOrBlank()){
            imageView.setImageResource(R.drawable.ic_launcher_background)
            return
        }

        if(!image.startsWith("http://")&&!image.startsWith("https://")&&!image.contains("/")){
            val imageName=image.substringBeforeLast(".").replace("-","_").lowercase()
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
}