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
import com.example.kpop.network.model.ApiProduct

class AdminProductAdapter(
    private var list: List<ApiProduct>,
    private val onSaleUpdate: (ApiProduct, Boolean, Double?) -> Unit,
    private val onVariants: (ApiProduct) -> Unit,
    private val onImages: (ApiProduct) -> Unit,
    private val onEdit: (ApiProduct) -> Unit,
    private val onDelete: (ApiProduct) -> Unit
) : RecyclerView.Adapter<AdminProductAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtAdminProductName)
        val info: TextView = view.findViewById(R.id.txtAdminProductInfo)
        val price: TextView = view.findViewById(R.id.txtAdminProductPrice)
        val saleSwitch: Switch = view.findViewById(R.id.switchProductSale)
        val salePrice: EditText = view.findViewById(R.id.etProductSalePrice)
        val btnSave: Button = view.findViewById(R.id.btnSaveProductSale)
        val btnVariants: Button = view.findViewById(R.id.btnManageVariants)
        val btnImages: Button = view.findViewById(R.id.btnManageImages)
        val btnEdit: Button = view.findViewById(R.id.btnEditProduct)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_product_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = list[position]

        holder.name.text = product.name
        holder.info.text = "${product.category?.name ?: "No Category"} • ${product.group?.name ?: "No Group"} • Stock ${product.quantity}"
        holder.price.text = if (product.onSale && product.salePrice != null) "RM%.2f  →  RM%.2f".format(product.price, product.salePrice) else "RM%.2f".format(product.price)

        holder.saleSwitch.setOnCheckedChangeListener(null)
        holder.saleSwitch.isChecked = product.onSale
        holder.salePrice.setText(product.salePrice?.toString() ?: "")
        holder.salePrice.isEnabled = product.onSale

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

            if (onSale && salePrice!! >= product.price) {
                holder.salePrice.error = "Sale price must be lower than normal price"
                return@setOnClickListener
            }

            if (salePrice != null) {
                if (onSale && salePrice <= 0) {
                    holder.salePrice.error = "Invalid sale price"
                    return@setOnClickListener
                }
            }

            onSaleUpdate(product, onSale, if (onSale) salePrice else null)
        }

        holder.btnVariants.setOnClickListener { onVariants(product) }
        holder.btnImages.setOnClickListener { onImages(product) }
        holder.btnEdit.setOnClickListener { onEdit(product) }
        holder.btnDelete.setOnClickListener { onDelete(product) }
    }

    fun updateList(newList: List<ApiProduct>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}