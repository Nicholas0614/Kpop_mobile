package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiVariant

class AdminVariantAdapter(
    private var list: List<ApiVariant>,
    private val onSaleUpdate: (ApiVariant, Boolean, Double?) -> Unit,
    private val onEdit: (ApiVariant) -> Unit,
    private val onDelete: (ApiVariant) -> Unit
) : RecyclerView.Adapter<AdminVariantAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtVariantName)
        val stock: TextView = view.findViewById(R.id.txtVariantStock)
        val price: TextView = view.findViewById(R.id.txtVariantPrice)
        val saleSwitch: Switch = view.findViewById(R.id.switchVariantSale)
        val salePrice: EditText = view.findViewById(R.id.etVariantSalePrice)
        val btnSave: Button = view.findViewById(R.id.btnSaveVariantSale)
        val btnEdit: Button = view.findViewById(R.id.btnEditVariant)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteVariant)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_variant_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val variant = list[position]

        holder.name.text = variant.name
        holder.stock.text = "Stock: ${variant.quantity}"

        holder.price.text = if (variant.onSale && variant.salePrice != null) {
            "RM%.2f  →  RM%.2f".format(variant.price, variant.salePrice)
        } else {
            "RM%.2f".format(variant.price)
        }

        holder.saleSwitch.setOnCheckedChangeListener(null)
        holder.saleSwitch.isChecked = variant.onSale
        holder.salePrice.setText(variant.salePrice?.toString() ?: "")
        holder.salePrice.isEnabled = variant.onSale

        holder.saleSwitch.setOnCheckedChangeListener { _, checked ->
            holder.salePrice.isEnabled = checked
            if (!checked) holder.salePrice.error = null
        }

        holder.btnSave.setOnClickListener {
            val onSale = holder.saleSwitch.isChecked
            val salePrice = holder.salePrice.text.toString().toDoubleOrNull()

            if (onSale && salePrice == null) {
                holder.salePrice.error = "Enter sale price"
                return@setOnClickListener
            }

            if (onSale && salePrice!! >= variant.price) {
                holder.salePrice.error = "Sale price must be lower than normal price"
                return@setOnClickListener
            }

            if (salePrice != null) {
                if (onSale && salePrice <= 0) {
                    holder.salePrice.error = "Invalid sale price"
                    return@setOnClickListener
                }
            }

            onSaleUpdate(variant, onSale, if (onSale) salePrice else null)
        }

        holder.btnEdit.setOnClickListener { onEdit(variant) }
        holder.btnDelete.setOnClickListener { onDelete(variant) }
    }

    fun updateList(newList: List<ApiVariant>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}