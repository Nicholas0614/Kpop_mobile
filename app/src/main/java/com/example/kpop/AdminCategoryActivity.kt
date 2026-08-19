package com.example.kpop

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminCategoryAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.CategoryApi
import com.example.kpop.network.model.ApiCategory
import com.example.kpop.network.model.CategoryRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminCategoriesActivity : AppCompatActivity() {

    private val categoryApi by lazy { RetrofitClient.create(this, CategoryApi::class.java) }
    private lateinit var adapter: AdminCategoryAdapter



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_categories)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminCategory)) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        if (!SessionManager(this).getRole().equals("admin", ignoreCase = true)) {
            finish()
            return
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        adapter = AdminCategoryAdapter(emptyList(), { showCategoryDialog(it) }, { confirmDelete(it) })

        findViewById<RecyclerView>(R.id.recyclerCategories).apply {
            layoutManager = LinearLayoutManager(this@AdminCategoriesActivity)
            adapter = this@AdminCategoriesActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.btnAddCategory).setOnClickListener {
            showCategoryDialog(null)
        }

        loadCategories()
    }

    private fun loadCategories() {
        categoryApi.getCategories().enqueue(object : Callback<List<ApiCategory>> {
            override fun onResponse(call: Call<List<ApiCategory>>, response: Response<List<ApiCategory>>) {
                if (response.isSuccessful) adapter.updateList(response.body() ?: emptyList())
                else Toast.makeText(this@AdminCategoriesActivity, "Unable to load categories", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<List<ApiCategory>>, t: Throwable) {
                Toast.makeText(this@AdminCategoriesActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showCategoryDialog(category: ApiCategory?) {
        val input = EditText(this)
        input.hint = "Category name"
        input.setText(category?.name ?: "")
        input.setPadding(40, 20, 40, 20)

        AlertDialog.Builder(this)
            .setTitle(if (category == null) "Add Category" else "Edit Category")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(this, "Enter category name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                saveCategory(category, name)
            }
            .show()
    }

    private fun saveCategory(category: ApiCategory?, name: String) {
        val request = CategoryRequest(name)
        val call = if (category == null) categoryApi.addCategory(request) else categoryApi.updateCategory(category.id, request)

        call.enqueue(object : Callback<ApiCategory> {
            override fun onResponse(call: Call<ApiCategory>, response: Response<ApiCategory>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminCategoriesActivity, response.errorBody()?.string() ?: "Unable to save category", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminCategoriesActivity, if (category == null) "Category added" else "Category updated", Toast.LENGTH_SHORT).show()
                loadCategories()
            }

            override fun onFailure(call: Call<ApiCategory>, t: Throwable) {
                Toast.makeText(this@AdminCategoriesActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmDelete(category: ApiCategory) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete ${category.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteCategory(category.id) }
            .show()
    }

    private fun deleteCategory(id: Int) {
        categoryApi.deleteCategory(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminCategoriesActivity, response.errorBody()?.string() ?: "Unable to delete category", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminCategoriesActivity, "Category deleted", Toast.LENGTH_SHORT).show()
                loadCategories()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminCategoriesActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}