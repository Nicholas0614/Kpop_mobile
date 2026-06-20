package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.model.Product

class AdminProductAdapter(
    private val productList: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAdminProduct: ImageView = itemView.findViewById(R.id.imgAdminProduct)
        val txtAdminName: TextView = itemView.findViewById(R.id.txtAdminName)
        val txtAdminCategory: TextView = itemView.findViewById(R.id.txtAdminCategory)
        val txtAdminPrice: TextView = itemView.findViewById(R.id.txtAdminPrice)
        val btnEditProduct: ImageButton = itemView.findViewById(R.id.btnEditProduct)
        val btnDeleteProduct: ImageButton = itemView.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_product_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]

        holder.imgAdminProduct.setImageResource(product.image)
        holder.txtAdminName.text = product.name
        holder.txtAdminCategory.text = product.category
        holder.txtAdminPrice.text = "RM${product.price}"

        holder.btnEditProduct.setOnClickListener {
            onEditClick(product)
        }

        holder.btnDeleteProduct.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.delete_cart_dialog, null)

            val dialog = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .create()

            val btnYes = dialogView.findViewById<Button>(R.id.btnConfirmDelete)
            val btnNo = dialogView.findViewById<Button>(R.id.btnCancel)

            btnYes.setOnClickListener {
                onDeleteClick(product)
                dialog.dismiss()
            }

            btnNo.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int = productList.size
}