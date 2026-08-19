package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiGroup

class AdminGroupAdapter(
    private var list: List<ApiGroup>,
    private val onEdit: (ApiGroup) -> Unit,
    private val onDelete: (ApiGroup) -> Unit
) : RecyclerView.Adapter<AdminGroupAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.txtGroupName)
        val company: TextView = view.findViewById(R.id.txtGroupCompany)
        val edit: Button = view.findViewById(R.id.btnEditGroup)
        val delete: Button = view.findViewById(R.id.btnDeleteGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.admin_group_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = list[position]

        holder.name.text = group.name
        holder.company.text = group.company ?: "No company"
        holder.edit.setOnClickListener { onEdit(group) }
        holder.delete.setOnClickListener { onDelete(group) }
    }

    fun updateList(newList: List<ApiGroup>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size
}