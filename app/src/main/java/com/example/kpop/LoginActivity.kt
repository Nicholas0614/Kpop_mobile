package com.example.kpop

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.User

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        db = AppDatabase.getDatabase(this)


        if (db.userDao().getAdminCount() == 0) {

            db.userDao().insertUser(
                User(
                    name = "Administrator",
                    email = "admin@gmail.com",
                    password = "admin123",
                    role = "admin"
                )
            )

        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegister = findViewById<TextView>(R.id.txtRegister)

        txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = db.userDao().login(email, password)

            if (user == null) {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            getSharedPreferences("user_session", MODE_PRIVATE)
                .edit()
                .putInt("userId", user.id)
                .putString("role", user.role)
                .putString("name", user.name)
                .apply()

            if (user.role == "admin") {
                startActivity(Intent(this, AdminActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }

            finish()
        }
    }
}