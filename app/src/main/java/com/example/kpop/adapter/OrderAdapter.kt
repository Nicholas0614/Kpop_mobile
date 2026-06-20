package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.database.ReviewDao
import com.example.kpop.model.Order

class OrderAdapter(
    private val orderList: MutableList<Order>,
    private val userId: Int,
    private val reviewDao: ReviewDao,
    private val onReviewClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtDate: TextView = itemView.findViewById(R.id.txtDate)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val txtTotal: TextView = itemView.findViewById(R.id.txtTotal)
        val btnReview: Button = itemView.findViewById(R.id.btnReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.purchase_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]

        holder.imgProduct.setImageResource(order.image)
        holder.txtName.text = order.name
        holder.txtDate.text = "Date: ${order.date}"
        holder.txtQuantity.text = "Quantity: ${order.quantity}"
        holder.txtTotal.text = "Total: RM${order.totalPrice}"

        val existingReview = reviewDao.getUserReview(userId, order.productId)

        if (existingReview != null) {
            holder.btnReview.text = "Reviewed ✅"
            holder.btnReview.isEnabled = false
        } else {
            holder.btnReview.text = "Review"
            holder.btnReview.isEnabled = true

            holder.btnReview.setOnClickListener {
                onReviewClick(order)
            }
        }
    }

    override fun getItemCount(): Int = orderList.size
}