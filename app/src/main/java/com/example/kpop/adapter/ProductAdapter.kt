package com.example.kpop.adapter

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kpop.R
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.model.ApiProduct

class ProductAdapter(
    productList: List<ApiProduct>,
    private val onItemClick: (ApiProduct) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private var dataList: List<ApiProduct> = productList

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val txtRating: TextView = itemView.findViewById(R.id.txtRating)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = dataList[position]

        loadImage(holder.imgProduct, product.image)

        holder.txtName.text = product.name

        val categoryName = product.category?.name ?: "Uncategorized"
        val groupName = product.group?.name

        holder.txtCategory.text = if (!groupName.isNullOrBlank()) "$categoryName • $groupName" else categoryName
        holder.txtRating.text = "⭐ %.1f".format(product.rating ?: 0.0)

        if (product.onSale && product.salePrice != null) {
            val normalPrice = "RM%.2f".format(product.price)
            val salePrice = "RM%.2f".format(product.salePrice)
            val priceText = SpannableString("$normalPrice  $salePrice")

            priceText.setSpan(StrikethroughSpan(), 0, normalPrice.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            priceText.setSpan(StyleSpan(Typeface.BOLD), normalPrice.length + 2, priceText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            holder.txtPrice.text = priceText
        } else {
            holder.txtPrice.text = "RM%.2f".format(product.price)
        }

        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    private fun loadImage(imageView: ImageView, image: String?) {
        val context = imageView.context

        if (image.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_launcher_background)
            return
        }

        if (!image.startsWith("http://") && !image.startsWith("https://") && !image.contains("/")) {
            val imageName = image.substringBeforeLast(".").replace("-", "_").lowercase()
            val imageRes = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            if (imageRes != 0) {
                imageView.setImageResource(imageRes)
                return
            }
        }

        Glide.with(context)
            .load(RetrofitClient.imageUrl(image))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }

    fun updateList(newList: List<ApiProduct>) {
        dataList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = dataList.size
}