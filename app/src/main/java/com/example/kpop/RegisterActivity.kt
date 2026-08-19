package com.example.kpop

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.api.AuthApi
import com.example.kpop.network.model.ApiUser
import com.example.kpop.network.model.RegisterRequest
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity:AppCompatActivity(){

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val scrollView=findViewById<NestedScrollView>(R.id.formScrollView)
        val nameLayout=findViewById<TextInputLayout>(R.id.nameLayout)
        val emailLayout=findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout=findViewById<TextInputLayout>(R.id.passwordLayout)
        val confirmPasswordLayout=findViewById<TextInputLayout>(R.id.confirmPasswordLayout)

        val etName=findViewById<TextInputEditText>(R.id.etName)
        val etEmail=findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword=findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword=findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val btnRegister=findViewById<Button>(R.id.btnRegister)
        val txtLogin=findViewById<TextView>(R.id.txtLogin)
        val btnBack=findViewById<ImageButton>(R.id.btnBack)

        ViewCompat.setOnApplyWindowInsetsListener(scrollView){v,insets->
            val ime=insets.getInsets(WindowInsetsCompat.Type.ime())
            val bars=insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft,v.paddingTop,v.paddingRight,maxOf(ime.bottom,bars.bottom)+20.dp())
            insets
        }

        fun bringIntoView(view:View){
            scrollView.postDelayed({
                val rect=Rect()
                view.getDrawingRect(rect)
                scrollView.offsetDescendantRectToMyCoords(view,rect)
                rect.bottom+=120.dp()
                scrollView.smoothScrollTo(0,rect.bottom-scrollView.height)
            },250)
        }

        etName.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}
        etEmail.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}
        etPassword.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}
        etConfirmPassword.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}

        btnBack.setOnClickListener{finish()}
        txtLogin.setOnClickListener{finish()}

        etName.doAfterTextChanged{if(!it.isNullOrBlank())nameLayout.error=null}
        etEmail.doAfterTextChanged{if(!it.isNullOrBlank())emailLayout.error=null}

        etPassword.doAfterTextChanged{
            if(!it.isNullOrBlank()){
                passwordLayout.error=null
                if(!etConfirmPassword.text.isNullOrBlank()&&etConfirmPassword.text.toString()==it.toString())confirmPasswordLayout.error=null
            }
        }

        etConfirmPassword.doAfterTextChanged{if(!it.isNullOrBlank())confirmPasswordLayout.error=null}

        btnRegister.setOnClickListener{
            val name=etName.text.toString().trim()
            val email=etEmail.text.toString().trim()
            val password=etPassword.text.toString()
            val confirm=etConfirmPassword.text.toString()

            nameLayout.error=null;emailLayout.error=null;passwordLayout.error=null;confirmPasswordLayout.error=null
            var valid=true

            if(name.isEmpty()){nameLayout.error="Please fill in your full name";valid=false}
            if(email.isEmpty()){emailLayout.error="Please fill in your email";valid=false}
            if(password.isEmpty()){passwordLayout.error="Please fill in your password";valid=false}
            if(confirm.isEmpty()){confirmPasswordLayout.error="Please confirm your password";valid=false}
            if(!valid)return@setOnClickListener

            if(password!=confirm){confirmPasswordLayout.error="Passwords do not match";return@setOnClickListener}

            btnRegister.isEnabled=false
            val request=RegisterRequest(name=name,email=email,password=password)

            RetrofitClient.create(this,AuthApi::class.java).register(request).enqueue(object:Callback<ApiUser>{
                override fun onResponse(call:Call<ApiUser>,response:Response<ApiUser>){
                    btnRegister.isEnabled=true

                    if(!response.isSuccessful){
                        val errorMessage=response.errorBody()?.string()?:"Registration failed."
                        Toast.makeText(this@RegisterActivity,errorMessage,Toast.LENGTH_LONG).show()
                        return
                    }

                    Toast.makeText(this@RegisterActivity,"Registration Successful! Please login.",Toast.LENGTH_SHORT).show()
                    finish()
                }

                override fun onFailure(call:Call<ApiUser>,t:Throwable){
                    btnRegister.isEnabled=true
                    Toast.makeText(this@RegisterActivity,"Cannot connect to server: ${t.message}",Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun Int.dp():Int=(this*resources.displayMetrics.density).toInt()
}