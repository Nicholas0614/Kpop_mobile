package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiOrder

class AdminOrderAdapter(
    private var list: List<ApiOrder>,
    private val onStatusUpdate: (ApiOrder, String) -> Unit,
    private val onTrackingUpdate: (ApiOrder, String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.ViewHolder>() {

    private val statuses = listOf("PENDING", "PROCESSING", "PACKED", "SHIPPED", "DELIVERED", "CANCELLED")

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtId: TextView = view.findViewById(R.id.txtAdminOrderId)
        val txtCustomer: TextView = view.findViewById(R.id.txtAdminCustomer)
        val txtPayment: TextView = view.findViewById(R.id.txtAdminPayment)
        val txtTotal: TextView = view.findViewById(R.id.txtAdminTotal)
        val spinner: Spinner = view.findViewById(R.id.spinnerOrderStatus)
        val btnStatus: Button = view.findViewById(R.id.btnUpdateStatus)
        val tracking: EditText = view.findViewById(R.id.etTrackingNumber)
        val btnTracking: Button = view.findViewById(R.id.btnUpdateTracking)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_order_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = list[position]

        holder.txtId.text = "Order #${order.id}"
        holder.txtCustomer.text = "${order.recipientName ?: "Customer"} • User ${order.userId}"
        holder.txtPayment.text = "Payment: ${order.paymentStatus ?: "PENDING"}"
        holder.txtTotal.text = "Total: RM%.2f".format(order.finalPrice)
        holder.tracking.setText(order.trackingNumber ?: "")

        val adapter = ArrayAdapter(holder.itemView.context, android.R.layout.simple_spinner_item, statuses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spinner.adapter = adapter

        val statusIndex = statuses.indexOf(order.orderStatus ?: "PENDING")
        if (statusIndex >= 0) holder.spinner.setSelection(statusIndex)

        holder.btnStatus.setOnClickListener { onStatusUpdate(order, holder.spinner.selectedItem.toString()) }
        holder.btnTracking.setOnClickListener { onTrackingUpdate(order, holder.tracking.text.toString().trim()) }
    }

    fun updateList(newList: List<ApiOrder>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}