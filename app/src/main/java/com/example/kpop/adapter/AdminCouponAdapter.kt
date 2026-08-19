package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiCoupon

class AdminCouponAdapter(private var list:List<ApiCoupon>,private val onEdit:(ApiCoupon)->Unit,private val onDelete:(ApiCoupon)->Unit):RecyclerView.Adapter<AdminCouponAdapter.ViewHolder>() {

    class ViewHolder(view:View):RecyclerView.ViewHolder(view) {
        val code:TextView=view.findViewById(R.id.txtCouponCode)
        val discount:TextView=view.findViewById(R.id.txtCouponDiscount)
        val minimum:TextView=view.findViewById(R.id.txtCouponMinimum)
        val expiry:TextView=view.findViewById(R.id.txtCouponExpiry)
        val status:TextView=view.findViewById(R.id.txtCouponStatus)
        val edit:Button=view.findViewById(R.id.btnEditCoupon)
        val delete:Button=view.findViewById(R.id.btnDeleteCoupon)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_coupon_item,parent,false))

    override fun onBindViewHolder(holder:ViewHolder,position:Int) {
        val coupon=list[position]
        holder.code.text=coupon.code
        holder.discount.text="${coupon.discountPercentage}% OFF"
        holder.minimum.text="Minimum: RM%.2f".format(coupon.minimumPurchase)
        holder.expiry.text="Expiry: ${coupon.expiryDate?.substringBefore("T") ?: "-"}"
        holder.status.text=if(coupon.active)"Active" else "Inactive"
        holder.edit.setOnClickListener{onEdit(coupon)}
        holder.delete.setOnClickListener{onDelete(coupon)}
    }

    override fun getItemCount():Int=list.size

    fun updateList(newList:List<ApiCoupon>){list=newList;notifyDataSetChanged()}
}