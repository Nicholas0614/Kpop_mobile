package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kpop.R
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.model.ApiProductImage

class AdminProductImageAdapter(
    private var list: List<ApiProductImage>,
    private val onDelete: (ApiProductImage) -> Unit
) : RecyclerView.Adapter<AdminProductImageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.txtImageNumber)
        val image: ImageView = view.findViewById(R.id.imgProductImage)
        val url: TextView = view.findViewById(R.id.txtImageUrl)
        val btnDelete: Button = view.findViewById(R.id.btnDeleteImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_product_image_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val productImage = list[position]

        holder.number.text = "Image ${position + 1}"
        holder.url.text = productImage.imageUrl

        Glide.with(holder.itemView.context)
            .load(RetrofitClient.imageUrl(productImage.imageUrl))
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.btnDelete.setOnClickListener { onDelete(productImage) }
    }

    fun updateList(newList: List<ApiProductImage>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}