package com.example.kpop.adapter

import android.app.Dialog
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kpop.R
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.model.ApiCartItem

class CartAdapter(private var cartList:MutableList<ApiCartItem>,private val onQuantityChange:(ApiCartItem,Int)->Unit,private val onDelete:(ApiCartItem)->Unit,private val onSelectionChange:()->Unit):RecyclerView.Adapter<CartAdapter.ViewHolder>(){

    private val selectedIds=mutableSetOf<Int>()

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val cbSelect:CheckBox=itemView.findViewById(R.id.cbSelectItem)
        val imgProduct:ImageView=itemView.findViewById(R.id.imgProduct)
        val txtName:TextView=itemView.findViewById(R.id.txtName)
        val txtCategory:TextView=itemView.findViewById(R.id.txtCategory)
        val txtPrice:TextView=itemView.findViewById(R.id.txtPrice)
        val txtQuantity:TextView=itemView.findViewById(R.id.txtQuantity)
        val btnMinus:ImageButton=itemView.findViewById(R.id.btnMinus)
        val btnPlus:ImageButton=itemView.findViewById(R.id.btnPlus)
        val btnDelete:ImageButton=itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int)=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.cart_item,parent,false))

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val item=cartList[position]

        loadImage(holder.imgProduct,item.image)
        holder.txtName.text=item.name
        holder.txtCategory.text=if(!item.variantName.isNullOrBlank())"${item.category?:""} • ${item.variantName}" else item.category?:""
        holder.txtQuantity.text=item.quantity.toString()

        if(item.onSale&&item.price<item.originalPrice){
            val original="RM%.2f".format(item.originalPrice)
            val sale="RM%.2f".format(item.price)
            val text=SpannableString("$original  $sale")
            text.setSpan(StrikethroughSpan(),0,original.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            text.setSpan(StyleSpan(Typeface.BOLD),original.length+2,text.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            holder.txtPrice.text=text
        }else holder.txtPrice.text="RM%.2f".format(item.price)

        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked=selectedIds.contains(item.id)

        holder.cbSelect.setOnCheckedChangeListener{_,checked->
            if(checked)selectedIds.add(item.id) else selectedIds.remove(item.id)
            onSelectionChange()
        }

        holder.btnPlus.setOnClickListener{onQuantityChange(item,item.quantity+1)}

        holder.btnMinus.setOnClickListener{
            if(item.quantity>1)onQuantityChange(item,item.quantity-1)
        }

        holder.btnDelete.setOnClickListener{
            val dialog=Dialog(holder.itemView.context)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.delete_cart_dialog)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            dialog.findViewById<Button>(R.id.btnCancel).setOnClickListener{dialog.dismiss()}
            dialog.findViewById<Button>(R.id.btnConfirmDelete).setOnClickListener{
                selectedIds.remove(item.id)
                onDelete(item)
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    fun updateList(newList:List<ApiCartItem>){
        cartList=newList.toMutableList()
        selectedIds.retainAll(newList.map{it.id}.toSet())
        notifyDataSetChanged()
    }

    fun selectAll(selected:Boolean){
        selectedIds.clear()
        if(selected)selectedIds.addAll(cartList.map{it.id})
        notifyDataSetChanged()
        onSelectionChange()
    }

    fun getSelectedItems():List<ApiCartItem> = cartList.filter{selectedIds.contains(it.id)}

    fun getSelectedIds():List<Int> = getSelectedItems().map{it.id}

    fun getSelectedCount():Int = selectedIds.size

    fun getSelectedTotalPrice():Double = getSelectedItems().sumOf{it.price*it.quantity}

    fun areAllSelected():Boolean = cartList.isNotEmpty()&&selectedIds.size==cartList.size

    override fun getItemCount():Int=cartList.size

    private fun loadImage(imageView:ImageView,image:String?){
        val context=imageView.context

        if(image.isNullOrBlank()){
            imageView.setImageResource(R.drawable.ic_launcher_background)
            return
        }

        if(!image.startsWith("http://")&&!image.startsWith("https://")&&!image.contains("/")){
            val name=image.substringBeforeLast(".").replace("-","_").lowercase()
            val resourceId=context.resources.getIdentifier(name,"drawable",context.packageName)

            if(resourceId!=0){
                imageView.setImageResource(resourceId)
                return
            }
        }

        Glide.with(context).load(RetrofitClient.imageUrl(image)).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(imageView)
    }
}