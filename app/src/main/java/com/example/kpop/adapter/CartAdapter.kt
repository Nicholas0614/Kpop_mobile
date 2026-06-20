package com.example.kpop.adapter

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.R
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.CartEntity

class CartAdapter(
    private val cartList: MutableList<CartEntity>,
    private val updateTotal: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
        val txtPrice: TextView = itemView.findViewById(R.id.txtPrice)
        val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        val btnMinus: ImageButton = itemView.findViewById(R.id.btnMinus)
        val btnPlus: ImageButton = itemView.findViewById(R.id.btnPlus)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = cartList[position]
        val db = AppDatabase.getDatabase(holder.itemView.context)

        holder.imgProduct.setImageResource(cartItem.image)
        holder.txtName.text = cartItem.name
        holder.txtCategory.text = cartItem.category
        holder.txtPrice.text = "RM%.2f".format(cartItem.price)
        holder.txtQuantity.text = cartItem.quantity.toString()

        holder.btnPlus.setOnClickListener {
            cartItem.quantity++
            db.cartDao().updateCartItem(cartItem)
            holder.txtQuantity.text = cartItem.quantity.toString()
            updateTotal()
        }

        holder.btnMinus.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                db.cartDao().updateCartItem(cartItem)
                holder.txtQuantity.text = cartItem.quantity.toString()
                updateTotal()
            }
        }

        holder.btnDelete.setOnClickListener {
            val currentPosition = holder.adapterPosition

            if (currentPosition != RecyclerView.NO_POSITION) {
                val dialog = Dialog(holder.itemView.context)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.delete_cart_dialog)
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
                val btnConfirmDelete = dialog.findViewById<Button>(R.id.btnConfirmDelete)

                btnCancel.setOnClickListener {
                    dialog.dismiss()
                }

                btnConfirmDelete.setOnClickListener {
                    val item = cartList[currentPosition]

                    db.cartDao().deleteCartItem(item)
                    cartList.removeAt(currentPosition)

                    notifyItemRemoved(currentPosition)
                    notifyItemRangeChanged(currentPosition, cartList.size)

                    updateTotal()
                    dialog.dismiss()
                }

                dialog.show()
            }
        }
    }

    override fun getItemCount(): Int = cartList.size

    fun getTotalPrice(): Double {
        var total = 0.0

        for (item in cartList) {
            total += item.price * item.quantity
        }

        return total
    }
}