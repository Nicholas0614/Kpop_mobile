package com.example.kpop

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kpop.adapter.AdminGroupAdapter
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.GroupApi
import com.example.kpop.network.model.ApiGroup
import com.example.kpop.network.model.GroupRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminGroupsActivity : AppCompatActivity() {

    private val groupApi by lazy { RetrofitClient.create(this, GroupApi::class.java) }
    private lateinit var adapter: AdminGroupAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_groups)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminGroup)) { v, insets ->
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

        adapter = AdminGroupAdapter(emptyList(), { showGroupDialog(it) }, { confirmDelete(it) })

        findViewById<RecyclerView>(R.id.recyclerGroups).apply {
            layoutManager = LinearLayoutManager(this@AdminGroupsActivity)
            adapter = this@AdminGroupsActivity.adapter
        }

        findViewById<FloatingActionButton>(R.id.btnAddGroup).setOnClickListener {
            showGroupDialog(null)
        }

        loadGroups()
    }

    private fun loadGroups() {
        groupApi.getGroups().enqueue(object : Callback<List<ApiGroup>> {
            override fun onResponse(call: Call<List<ApiGroup>>, response: Response<List<ApiGroup>>) {
                if (response.isSuccessful) adapter.updateList(response.body() ?: emptyList())
                else Toast.makeText(this@AdminGroupsActivity, "Unable to load groups", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<List<ApiGroup>>, t: Throwable) {
                Toast.makeText(this@AdminGroupsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun showGroupDialog(group: ApiGroup?) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 10, 40, 10)

        val name = EditText(this)
        val company = EditText(this)
        val debutDate = EditText(this)
        val image = EditText(this)
        val description = EditText(this)

        name.hint = "Group name"
        company.hint = "Company"
        debutDate.hint = "Debut date (YYYY-MM-DD)"
        image.hint = "Image"
        description.hint = "Description"

        description.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        description.minLines = 3

        name.setText(group?.name ?: "")
        company.setText(group?.company ?: "")
        debutDate.setText(group?.debutDate ?: "")
        image.setText(group?.image ?: "")
        description.setText(group?.description ?: "")

        layout.addView(name)
        layout.addView(company)
        layout.addView(debutDate)
        layout.addView(image)
        layout.addView(description)

        AlertDialog.Builder(this)
            .setTitle(if (group == null) "Add K-pop Group" else "Edit K-pop Group")
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val groupName = name.text.toString().trim()

                if (groupName.isEmpty()) {
                    Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val request = GroupRequest(
                    name = groupName,
                    company = company.text.toString().trim().ifEmpty { null },
                    debutDate = debutDate.text.toString().trim().ifEmpty { null },
                    image = image.text.toString().trim().ifEmpty { null },
                    description = description.text.toString().trim().ifEmpty { null }
                )

                saveGroup(group, request)
            }
            .show()
    }

    private fun saveGroup(group: ApiGroup?, request: GroupRequest) {
        val call = if (group == null) groupApi.addGroup(request) else groupApi.updateGroup(group.id, request)

        call.enqueue(object : Callback<ApiGroup> {
            override fun onResponse(call: Call<ApiGroup>, response: Response<ApiGroup>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminGroupsActivity, response.errorBody()?.string() ?: "Unable to save group", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminGroupsActivity, if (group == null) "Group added" else "Group updated", Toast.LENGTH_SHORT).show()
                loadGroups()
            }

            override fun onFailure(call: Call<ApiGroup>, t: Throwable) {
                Toast.makeText(this@AdminGroupsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun confirmDelete(group: ApiGroup) {
        AlertDialog.Builder(this)
            .setTitle("Delete Group")
            .setMessage("Delete ${group.name}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ -> deleteGroup(group.id) }
            .show()
    }

    private fun deleteGroup(id: Int) {
        groupApi.deleteGroup(id).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Toast.makeText(this@AdminGroupsActivity, response.errorBody()?.string() ?: "Unable to delete group", Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@AdminGroupsActivity, "Group deleted", Toast.LENGTH_SHORT).show()
                loadGroups()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AdminGroupsActivity, "Server error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}