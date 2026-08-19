package com.example.kpop

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kpop.network.RetrofitClient
import com.example.kpop.network.SessionManager
import com.example.kpop.network.api.AddressApi
import com.example.kpop.network.model.AddressRequest
import com.example.kpop.network.model.ApiAddress
import com.google.android.material.materialswitch.MaterialSwitch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAddressActivity:AppCompatActivity(){

    private val addressApi by lazy{RetrofitClient.create(this,AddressApi::class.java)}

    private lateinit var etLabel:EditText
    private lateinit var etName:EditText
    private lateinit var etPhone:EditText
    private lateinit var etAddress1:EditText
    private lateinit var etAddress2:EditText
    private lateinit var etPostcode:EditText
    private lateinit var etCity:EditText
    private lateinit var etState:EditText
    private lateinit var etCountry:EditText
    private lateinit var switchDefault:MaterialSwitch
    private lateinit var btnSave:Button

    private var addressId=0

    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_address)

        addressId=intent.getIntExtra("addressId",0)

        if(addressId==0){
            finish()
            return
        }

        etLabel=findViewById(R.id.etLabel)
        etName=findViewById(R.id.etRecipientName)
        etPhone=findViewById(R.id.etPhone)
        etAddress1=findViewById(R.id.etAddressLine1)
        etAddress2=findViewById(R.id.etAddressLine2)
        etPostcode=findViewById(R.id.etPostcode)
        etCity=findViewById(R.id.etCity)
        etState=findViewById(R.id.etState)
        etCountry=findViewById(R.id.etCountry)
        switchDefault=findViewById(R.id.switchDefault)
        btnSave=findViewById(R.id.btnSaveAddress)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener{finish()}
        btnSave.setOnClickListener{saveAddress()}

        loadAddress()
    }

    private fun loadAddress(){
        addressApi.getAddress(addressId).enqueue(object:Callback<ApiAddress>{
            override fun onResponse(call:Call<ApiAddress>,response:Response<ApiAddress>){
                val address=response.body()

                if(!response.isSuccessful||address==null){
                    Toast.makeText(this@EditAddressActivity,"Unable to load address",Toast.LENGTH_SHORT).show()
                    return
                }

                etLabel.setText(address.label)
                etName.setText(address.recipientName)
                etPhone.setText(address.phone)
                etAddress1.setText(address.addressLine1)
                etAddress2.setText(address.addressLine2?:"")
                etPostcode.setText(address.postcode)
                etCity.setText(address.city)
                etState.setText(address.state)
                etCountry.setText(address.country)
                switchDefault.isChecked=address.defaultAddress
            }

            override fun onFailure(call:Call<ApiAddress>,t:Throwable){
                Toast.makeText(this@EditAddressActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveAddress(){
        val label=etLabel.text.toString().trim()
        val name=etName.text.toString().trim()
        val phone=etPhone.text.toString().trim()
        val address1=etAddress1.text.toString().trim()
        val address2=etAddress2.text.toString().trim()
        val postcode=etPostcode.text.toString().trim()
        val city=etCity.text.toString().trim()
        val state=etState.text.toString().trim()
        val country=etCountry.text.toString().trim()

        if(label.isEmpty()){
            etLabel.error="Enter address label"
            return
        }

        if(name.isEmpty()){
            etName.error="Enter recipient name"
            return
        }

        if(phone.isEmpty()){
            etPhone.error="Enter phone number"
            return
        }

        if(address1.isEmpty()){
            etAddress1.error="Enter address"
            return
        }

        if(postcode.isEmpty()){
            etPostcode.error="Enter postcode"
            return
        }

        if(city.isEmpty()){
            etCity.error="Enter city"
            return
        }

        if(state.isEmpty()){
            etState.error="Enter state"
            return
        }

        if(country.isEmpty()){
            etCountry.error="Enter country"
            return
        }

        val request=AddressRequest(
            userId=SessionManager(this).getUserId(),
            label=label,
            recipientName=name,
            phone=phone,
            addressLine1=address1,
            addressLine2=address2.ifEmpty{null},
            city=city,
            state=state,
            postcode=postcode,
            country=country,
            defaultAddress=switchDefault.isChecked
        )

        btnSave.isEnabled=false

        addressApi.updateAddress(addressId,request).enqueue(object:Callback<ApiAddress>{
            override fun onResponse(call:Call<ApiAddress>,response:Response<ApiAddress>){
                btnSave.isEnabled=true

                if(!response.isSuccessful){
                    Toast.makeText(this@EditAddressActivity,response.errorBody()?.string()?:"Unable to update address",Toast.LENGTH_LONG).show()
                    return
                }

                Toast.makeText(this@EditAddressActivity,"Address updated",Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }

            override fun onFailure(call:Call<ApiAddress>,t:Throwable){
                btnSave.isEnabled=true
                Toast.makeText(this@EditAddressActivity,"Server error: ${t.message}",Toast.LENGTH_LONG).show()
            }
        })
    }
}