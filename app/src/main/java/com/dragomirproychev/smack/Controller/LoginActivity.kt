package com.dragomirproychev.smack.Controller

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dragomirproychev.smack.R
import com.dragomirproychev.smack.Utilities.REQUEST_EXIT

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun loginLoginButtonClicked(view: View){

    }

    fun loginCreateUserButtonClicked(view: View){
        val createUserIntent = Intent(this,
                CreateUserActivity:: class.java)
        startActivityForResult(createUserIntent, REQUEST_EXIT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_EXIT){
            if(resultCode == RESULT_OK){
                finish()
            }
        }
    }
}
