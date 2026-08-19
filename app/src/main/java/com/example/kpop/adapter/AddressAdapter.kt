package com.example.kpop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.network.model.ApiAddress

class AddressAdapter(
    private var addresses:List<ApiAddress>,
    private val onSelect:(ApiAddress)->Unit,
    private val onEdit:(ApiAddress)->Unit
):RecyclerView.Adapter<AddressAdapter.ViewHolder>(){

    private var selectedId:Int?=null

    class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val cbSelect:AppCompatCheckBox=view.findViewById(R.id.cbSelectAddress)
        val label:TextView=view.findViewById(R.id.txtAddressLabel)
        val defaultLabel:TextView=view.findViewById(R.id.txtDefault)
        val name:TextView=view.findViewById(R.id.txtRecipientName)
        val phone:TextView=view.findViewById(R.id.txtPhone)
        val address:TextView=view.findViewById(R.id.txtFullAddress)
        val edit:TextView=view.findViewById(R.id.btnEditAddress)
    }

    override fun onCreateViewHolder(parent:ViewGroup,viewType:Int):ViewHolder{
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.address_item,parent,false))
    }

    override fun onBindViewHolder(holder:ViewHolder,position:Int){
        val item=addresses[position]

        holder.label.text=item.label
        holder.name.text=item.recipientName
        holder.phone.text=item.phone

        val line2=if(item.addressLine2.isNullOrBlank())"" else "\n${item.addressLine2}"
        holder.address.text="${item.addressLine1}$line2\n${item.postcode} ${item.city}, ${item.state}\n${item.country}"

        holder.defaultLabel.visibility=if(item.defaultAddress)View.VISIBLE else View.GONE

        holder.cbSelect.setOnCheckedChangeListener(null)
        holder.cbSelect.isChecked=selectedId==item.id

        holder.cbSelect.setOnClickListener{
            selectedId=item.id
            notifyDataSetChanged()
            onSelect(item)
        }

        holder.itemView.setOnClickListener{
            selectedId=item.id
            notifyDataSetChanged()
            onSelect(item)
        }

        holder.edit.setOnClickListener{
            onEdit(item)
        }
    }

    fun updateList(newList:List<ApiAddress>){
        addresses=newList
        notifyDataSetChanged()
    }

    fun setSelectedAddress(id:Int){
        selectedId=id
        notifyDataSetChanged()
    }

    override fun getItemCount():Int=addresses.size
}