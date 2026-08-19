package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.UserCoupon

class CouponAdapter(private var list:List<UserCoupon>,private val selectMode:Boolean=false,private val onAction:(UserCoupon)->Unit):RecyclerView.Adapter<CouponAdapter.ViewHolder>(){

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val code:TextView=view.findViewById(R.id.txtCouponCode)
        val discount:TextView=view.findViewById(R.id.txtCouponDiscount)
        val minimum:TextView=view.findViewById(R.id.txtMinimum)
        val expiry:TextView=view.findViewById(R.id.txtExpiry)
        val button:Button=view.findViewById(R.id.btnSaveCoupon)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int)=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.coupon_item,parent,false))

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val coupon=list[position]
        holder.code.text=coupon.code
        holder.discount.text="${formatNumber(coupon.discountPercentage)}% OFF"
        holder.minimum.text="Min. spend RM%.2f".format(coupon.minimumPurchase)
        holder.expiry.text="Valid until ${coupon.expiryDate.substringBefore("T")}"

        if(selectMode){
            holder.button.text="Use Voucher"
            holder.button.isEnabled=true
        }else{
            holder.button.text=if(coupon.saved)"Saved" else "Save"
            holder.button.isEnabled=true
        }

        holder.button.setOnClickListener{onAction(coupon)}
        if(selectMode)holder.itemView.setOnClickListener{onAction(coupon)}
    }

    override fun getItemCount()=list.size

    fun updateList(newList:List<UserCoupon>){list=newList;notifyDataSetChanged()}

    private fun formatNumber(value:Double):String=if(value%1.0==0.0)value.toInt().toString() else "%.2f".format(value)
}