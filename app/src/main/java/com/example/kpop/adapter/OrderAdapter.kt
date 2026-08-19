package com.example.kpop.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiOrder
import com.example.kpop.network.model.ApiOrderItem

class OrderAdapter(private var list:List<ApiOrder>,private val onReceived:(ApiOrder)->Unit,private val onReview:(ApiOrderItem)->Unit):RecyclerView.Adapter<OrderAdapter.ViewHolder>(){

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val txtOrderId:TextView=view.findViewById(R.id.txtOrderId)
        val txtOrderStatus:TextView=view.findViewById(R.id.txtOrderStatus)
        val txtDate:TextView=view.findViewById(R.id.txtDate)
        val itemsContainer:LinearLayout=view.findViewById(R.id.itemsContainer)
        val txtSubtotal:TextView=view.findViewById(R.id.txtSubtotal)
        val txtDiscount:TextView=view.findViewById(R.id.txtDiscount)
        val txtTotal:TextView=view.findViewById(R.id.txtTotal)
        val txtPaymentStatus:TextView=view.findViewById(R.id.txtPaymentStatus)
        val txtTracking:TextView=view.findViewById(R.id.txtTracking)
        val txtAddress:TextView=view.findViewById(R.id.txtAddress)
        val btnOrderReceived:Button=view.findViewById(R.id.btnOrderReceived)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int)=ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.purchase_item,parent,false))

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val order=list[position]

        holder.txtOrderId.text="Order #${order.id}"
        holder.txtOrderStatus.text=order.orderStatus?:"PENDING"
        holder.txtDate.text="Date: ${formatDate(order.date)}"

        holder.itemsContainer.removeAllViews()

        order.items.forEach{item->
            val row=LinearLayout(holder.itemView.context).apply{
                orientation=LinearLayout.VERTICAL
                setPadding(0,8,0,12)
            }

            val info=TextView(holder.itemView.context).apply{
                val variant=if(item.variantName.isNullOrBlank())"" else " • ${item.variantName}"
                text="${item.productName}$variant\n${item.quantity} × RM%.2f".format(item.price)
                textSize=15f
                setTextColor(0xFF333333.toInt())
            }

            row.addView(info)

            if(order.orderStatus.equals("RECEIVED",true)){
                val review=Button(holder.itemView.context).apply{
                    text="Review"
                    isAllCaps=false
                    setOnClickListener{onReview(item)}
                }
                row.addView(review)
            }

            holder.itemsContainer.addView(row)
        }

        holder.txtSubtotal.text="Subtotal: RM%.2f".format(order.totalPrice)

        if(order.discountAmount>0){
            holder.txtDiscount.visibility=View.VISIBLE
            holder.txtDiscount.text="Discount: -RM%.2f${if(!order.couponCode.isNullOrBlank())" (${order.couponCode})" else ""}".format(order.discountAmount)
        }else holder.txtDiscount.visibility=View.GONE

        holder.txtTotal.text="Total: RM%.2f".format(order.finalPrice)
        holder.txtPaymentStatus.text="Payment: ${order.paymentStatus?:"PENDING"}"
        holder.txtTracking.text=if(order.trackingNumber.isNullOrBlank())"Tracking: Not available yet" else "Tracking: ${order.trackingNumber}"

        val address=listOfNotNull(order.addressLine1,order.addressLine2,order.postcode,order.city,order.state,order.country).filter{it.isNotBlank()}.joinToString(", ")
        holder.txtAddress.text=if(address.isBlank())"" else "${order.recipientName?:""}\n${order.phone?:""}\n$address"

        val canReceive=order.paymentStatus.equals("PAID",true)&&order.orderStatus.equals("DELIVERED",true)
        holder.btnOrderReceived.visibility=if(canReceive)View.VISIBLE else View.GONE
        holder.btnOrderReceived.setOnClickListener{onReceived(order)}
    }

    fun updateList(newList:List<ApiOrder>){list=newList;notifyDataSetChanged()}

    private fun formatDate(date:String?):String{
        if(date.isNullOrBlank())return "-"
        return date.replace("T"," ").substringBefore(".")
    }

    override fun getItemCount()=list.size
}