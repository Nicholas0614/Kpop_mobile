package com.example.kpop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.database.AppDatabase
import com.example.kpop.model.User

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)



        db = AppDatabase.getDatabase(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val txtLogin = findViewById<TextView>(R.id.txtLogin)

        btnRegister.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirm = etConfirmPassword.text.toString()

            if (name.isEmpty() ||
                email.isEmpty() ||
                password.isEmpty() ||
                confirm.isEmpty()
            ) {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (db.userDao().checkEmailExists(email) > 0) {
                Toast.makeText(this, "Email already exists.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.userDao().insertUser(
                User(
                    name = name,
                    email = email,
                    password = password,
                    role = "user"
                )
            )

            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
            finish()
        }

        txtLogin.setOnClickListener {
            finish()
        }
    }
}