package com.dragomirproychev.smack.Controller

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.dragomirproychev.smack.R
import com.dragomirproychev.smack.Services.AuthService
import com.dragomirproychev.smack.Utilities.BROADCAST_USER_DATA_CHANGE
import com.dragomirproychev.smack.Utilities.RESULT_FINISH
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private val random = Random()

    private var userAvatar = "profileDefault"
    private var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
        createSpinner.visibility = View.INVISIBLE
    }

    fun generateUserAvatar(view: View) {
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        userAvatar = if (color == 0) {
            "light$avatar"
        } else {
            "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar,
                "drawable", packageName)

        createAvatarImageView.setImageResource(resourceId)
    }

    fun generateColorClicked(view: View) {
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r, g, b))

        val savedR = r.toDouble() / 255;
        val savedG = g.toDouble() / 255;
        val savedB = b.toDouble() / 255;

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserButtonClicked(view: View) {
        enableSpinner(true)
        val userName = createUserUserNameText.text.toString()
        val email = createEmailText.text.toString()
        val password = createPasswordText.text.toString()

        if (userName.isEmpty() || email.isEmpty() ||
                password.isEmpty()) {
            Toast.makeText(this, "Make sure username, email and password are filled in.", Toast.LENGTH_SHORT).show()
            enableSpinner(false)
            return
        }

        AuthService.registerUser(
                email,
                password) { registerSuccess ->
            if (registerSuccess) {
                AuthService.loginUser(email,
                        password) { loginSuccess ->
                    if (loginSuccess) {
                        AuthService.createUser(userName, email, userAvatar, avatarColor) { createSuccess ->
                            if (createSuccess) {

                                val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)

                                enableSpinner(false)

                                setResult(RESULT_FINISH, null)
                                finish()
                            } else {
                                errorToast()
                            }
                        }
                    } else {
                        errorToast()
                    }
                }
            } else {
                errorToast()
            }
        }
    }

    private fun errorToast() {
        Toast.makeText(this, "Something went wrong. Please try again.", Toast.LENGTH_SHORT).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            createSpinner.visibility = View.VISIBLE
        } else {
            createSpinner.visibility = View.INVISIBLE
        }
        createUserButton.isEnabled = !createUserButton.isEnabled
        createAvatarImageView.isEnabled = !createAvatarImageView.isEnabled
        backgrounColorButton.isEnabled = !backgrounColorButton.isEnabled
    }
}
