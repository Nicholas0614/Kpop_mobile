package com.example.kpop

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AuthApi
import com.example.kpop.network.model.LoginRequest
import com.example.kpop.network.model.LoginResponse
import com.google.android.material.textfield.TextInputLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity:AppCompatActivity(){

    private lateinit var sessionManager:SessionManager

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        sessionManager=SessionManager(this)

        val scrollView=findViewById<NestedScrollView>(R.id.formScrollView)
        val emailLayout=findViewById<TextInputLayout>(R.id.emailLayout)
        val passwordLayout=findViewById<TextInputLayout>(R.id.passwordLayout)
        val etEmail=findViewById<EditText>(R.id.etEmail)
        val etPassword=findViewById<EditText>(R.id.etPassword)
        val btnLogin=findViewById<Button>(R.id.btnLogin)
        val txtRegister=findViewById<TextView>(R.id.txtRegister)
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
                rect.bottom+=100.dp()
                scrollView.smoothScrollTo(0,rect.bottom-scrollView.height)
            },250)
        }

        etEmail.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}
        etPassword.setOnFocusChangeListener{v,focused->if(focused)bringIntoView(v)}

        txtRegister.setOnClickListener{startActivity(Intent(this,RegisterActivity::class.java))}
        btnBack.setOnClickListener{finish()}

        etEmail.doAfterTextChanged{if(!it.isNullOrBlank())emailLayout.error=null}
        etPassword.doAfterTextChanged{if(!it.isNullOrBlank())passwordLayout.error=null}

        btnLogin.setOnClickListener{
            val email=etEmail.text.toString().trim()
            val password=etPassword.text.toString()

            emailLayout.error=null;passwordLayout.error=null
            var valid=true

            if(email.isEmpty()){emailLayout.error="Please fill in your email";valid=false}
            if(password.isEmpty()){passwordLayout.error="Please fill in your password";valid=false}
            if(!valid)return@setOnClickListener

            btnLogin.isEnabled=false
            val request=LoginRequest(email=email,password=password)

            RetrofitClient.create(this,AuthApi::class.java).login(request).enqueue(object:Callback<LoginResponse>{
                override fun onResponse(call:Call<LoginResponse>,response:Response<LoginResponse>){
                    btnLogin.isEnabled=true

                    if(!response.isSuccessful){passwordLayout.error="Invalid email or password";return}

                    val result=response.body()
                    if(result==null){Toast.makeText(this@LoginActivity,"Login failed.",Toast.LENGTH_SHORT).show();return}

                    sessionManager.saveLogin(result.token,result.user)
                    Toast.makeText(this@LoginActivity,"Welcome ${result.user.name}",Toast.LENGTH_SHORT).show()

                    if(result.user.role.equals("admin",ignoreCase=true)){
                        val intent=Intent(this@LoginActivity,AdminActivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent);finish()
                    }else{
                        startActivity(Intent(this@LoginActivity,MainActivity::class.java));finish()
                    }
                }

                override fun onFailure(call:Call<LoginResponse>,t:Throwable){
                    btnLogin.isEnabled=true
                    Toast.makeText(this@LoginActivity,"Cannot connect to server: ${t.message}",Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun Int.dp():Int=(this*resources.displayMetrics.density).toInt()
}