package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.model.Product

class ProductAdapter(
    private val productList: List<Product>,
    private val onItemClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var dataList: List<Product> = productList

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val txtRating: TextView = itemView.findViewById(R.id.txtRating)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item, parent, false)

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = dataList[position]

        holder.imgProduct.setImageResource(product.image)
        holder.txtName.text = product.name
        holder.txtCategory.text = product.category
        holder.txtRating.text = "⭐ ${product.rating}"
        holder.txtPrice.text = "RM%.2f".format(product.price)


        holder.itemView.setOnClickListener {
            onItemClick(product)
        }
    }

    fun updateList(newList: List<Product>) {
        dataList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}