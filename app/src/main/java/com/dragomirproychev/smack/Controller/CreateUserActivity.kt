package com.dragomirproychev.smack.Controller

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dragomirproychev.smack.R
import com.dragomirproychev.smack.Services.AuthService
import kotlinx.android.synthetic.main.activity_create_user.*
import java.util.*

class CreateUserActivity : AppCompatActivity() {

    private val random = Random()

    private var userAvatar = "profileDefault"
    private var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)
    }

    fun generateUserAvatar(view: View){
        val color = random.nextInt(2)
        val avatar = random.nextInt(28)

        userAvatar = if(color == 0) {
            "light$avatar"
        } else {
            "dark$avatar"
        }

        val resourceId = resources.getIdentifier(userAvatar,
                "drawable",packageName)

        createAvatarImageView.setImageResource(resourceId)
    }

    fun generateColorClicked(view: View){
        val r = random.nextInt(255)
        val g = random.nextInt(255)
        val b = random.nextInt(255)

        createAvatarImageView.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble() / 255;
        val savedG = g.toDouble() / 255;
        val savedB = b.toDouble() / 255;

        avatarColor = "[$savedR, $savedG, $savedB, 1]"
    }

    fun createUserButtonClicked(view: View){
        AuthService.registerUser(this,
               createEmailText.text.toString(),
                createPasswordText.text.toString()) {

        }
    }
}
