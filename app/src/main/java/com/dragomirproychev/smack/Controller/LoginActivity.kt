package com.dragomirproychev.smack.Controller

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.dragomirproychev.smack.R
import com.dragomirproychev.smack.Services.AuthService
import com.dragomirproychev.smack.Utilities.REQUEST_EXIT
import com.dragomirproychev.smack.Utilities.RESULT_FINISH
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginSpinner.visibility = View.INVISIBLE
    }

    fun loginLoginButtonClicked(view: View) {
        enableSpinner(true)
        hideKeyboard()

        val email = loginEmailText.text.toString()
        val password = loginPasswordText.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please fill in both email and password.", Toast.LENGTH_SHORT).show()
        }

        AuthService.loginUser(this, email, password) { loginSuccess ->
            if (loginSuccess) {
                AuthService.findUserByEmail(this) { findSuccess ->
                    if (findSuccess) {
                        enableSpinner(false)
                        finish()
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
            }
        }
    }

    fun loginCreateUserButtonClicked(view: View) {
        val createUserIntent = Intent(this,
                CreateUserActivity::class.java)
        startActivityForResult(createUserIntent, REQUEST_EXIT)
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginLoginButton.isEnabled = !loginLoginButton.isEnabled
        loginCreateUserButton.isEnabled = !loginCreateUserButton.isEnabled

    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken,0)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_EXIT) {
            if (resultCode == RESULT_FINISH) {
                finish()
            }
        }
    }
}
