package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiCategory

class AdminCategoryAdapter(
    private var list: List<ApiCategory>,
    private val onEdit: (ApiCategory) -> Unit,
    private val onDelete: (ApiCategory) -> Unit
) : RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtCategoryName)
        val more: ImageButton = view.findViewById(R.id.btnCategoryMore)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.admin_category_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val category = list[position]

        holder.name.text = category.name

        holder.more.setOnClickListener {
            val popup = PopupMenu(
                holder.itemView.context,
                holder.more
            )

            popup.menuInflater.inflate(
                R.menu.category_item_menu,
                popup.menu
            )

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    R.id.menuEditCategory -> {
                        onEdit(category)
                        true
                    }

                    R.id.menuDeleteCategory -> {
                        onDelete(category)
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }
    }

    fun updateList(newList: List<ApiCategory>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}