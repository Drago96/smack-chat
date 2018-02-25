package com.dragomirproychev.smack.Controller

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import com.dragomirproychev.smack.Adapters.MessageAdapter
import com.dragomirproychev.smack.Model.Channel
import com.dragomirproychev.smack.Model.Message
import com.dragomirproychev.smack.R
import com.dragomirproychev.smack.Services.AuthService
import com.dragomirproychev.smack.Services.MessageService
import com.dragomirproychev.smack.Services.UserDataService
import com.dragomirproychev.smack.Utilities.*
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {

    private val socket = IO.socket(SOCKET_URL)
    private var selectedChannel: Channel? = null

    lateinit var channelAdapter: ArrayAdapter<Channel>
    lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)

        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        socket.connect()
        socket.on(SOCKET_CHANNEL_CREATED, onNewChannel)
        socket.on(SOCKET_MESSAGE_CREATED, onNewMessage)

        LocalBroadcastManager.getInstance(this).registerReceiver(userDataChangeReceiver, IntentFilter(BROADCAST_USER_DATA_CHANGE))
        setUpAdapters()

        channel_list.setOnItemClickListener { _, _, i, _ ->
            selectedChannel = MessageService.channels[i]
            drawer_layout.closeDrawer(GravityCompat.START)
            updateWithChannel()
        }

        if (App.prefs.isLoggedIn) {
            AuthService.findUserByEmail(this) {}
        }

    }

    override fun onDestroy() {
        socket.disconnect()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangeReceiver)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun loginButtonNavClicked(view: View) {

        if (App.prefs.isLoggedIn) {
            UserDataService.logout()
            userNameNavHeader.text = ""
            userEmailNavHeader.text = ""
            userImageNavHeader.setImageResource(R.drawable.profiledefault)
            userImageNavHeader.setBackgroundColor(Color.TRANSPARENT)
            loginButtonNavHeader.text = "Login"

            mainChannelName.text = "Please Log In"

            channelAdapter.notifyDataSetChanged()
            updateMessages()

        } else {
            val loginActivityIntent = Intent(this,
                    LoginActivity::class.java)
            startActivity(loginActivityIntent)
        }

    }

    fun addChannelClicked(view: View) {
        if (!App.prefs.isLoggedIn) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.add_channel_dialog, null)

        builder.setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    val nameTextField = dialogView.findViewById<EditText>(R.id.addChannelNameText)
                    val descTextField = dialogView.findViewById<EditText>(R.id.addChannelDescriptionText)

                    val channelName = nameTextField.text.toString()
                    val channelDesc = descTextField.text.toString()

                    if (channelName.isNotEmpty()) {
                        socket.emit(SOCKET_NEW_CHANNEL, channelName, channelDesc)
                    } else {
                        Toast.makeText(this, "Can't create a channel without a name", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton("Cancel") { dialogInterface, i ->

                }
                .show()
    }

    fun sendMessageButtonClicked(view: View) {
        if (App.prefs.isLoggedIn && messageTextField.text.isNotEmpty() && selectedChannel != null) {

            val message = messageTextField.text.toString()
            val userId = UserDataService.id
            val channelId = selectedChannel?.id
            val userName = UserDataService.name
            val avatarName = UserDataService.avatarName
            val avatarColor = UserDataService.avatarColor

            socket.emit(SOCKET_NEW_MESSAGE, message, userId, channelId,
                    userName, avatarName, avatarColor)

            messageTextField.text.clear()
            hideKeyboard()
        } else {
            Toast.makeText(this, "Can't send message", Toast.LENGTH_SHORT).show()
        }
    }

    private val userDataChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (App.prefs.isLoggedIn) {
                userNameNavHeader.text = UserDataService.name
                userEmailNavHeader.text = UserDataService.email
                val resourceId = resources.getIdentifier(UserDataService.avatarName, "drawable", packageName)
                userImageNavHeader.setImageResource(resourceId)
                userImageNavHeader.setBackgroundColor(UserDataService.returnAvatarColor(UserDataService.avatarColor))
                loginButtonNavHeader.text = "Logout"

                Thread{
                    MessageService.getChannels { complete ->
                        if (complete) {
                            if (MessageService.channels.count() > 0) {
                                selectedChannel = MessageService.channels[0]
                                channelAdapter.notifyDataSetChanged()
                                updateWithChannel()
                            } else {
                                mainChannelName.text = "No Channel Selected"
                            }
                        }
                    }
                }.start()
            }
        }
    }

    private fun updateWithChannel() {
        mainChannelName.text = "#${selectedChannel?.name}"

        Thread{
            selectedChannel?.let {
                MessageService.getMessages(it.id) { complete ->
                    if (complete) {
                        updateMessages()
                    }
                }
            }
        }.start()
    }

    private fun setUpAdapters() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessageAdapter(this, MessageService.messages)
        messageListView.adapter = messageAdapter
        val layoutManager = LinearLayoutManager(this)
        messageListView.layoutManager = layoutManager
    }

    private val onNewChannel = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelName = args[0] as String
                val channelDescription = args[1] as String
                val channelId = args[2] as String

                val newChannel = Channel(channelName, channelDescription, channelId)

                MessageService.channels.add(newChannel)
                channelAdapter.notifyDataSetChanged()
            }
        }
    }

    private val onNewMessage = Emitter.Listener { args ->
        if (App.prefs.isLoggedIn) {
            runOnUiThread {
                val channelId = args[2] as String

                if (channelId == selectedChannel?.id) {
                    val messageBody = args[0] as String
                    val userName = args[3] as String
                    val userAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    val newMessage = Message(messageBody, userName, channelId, userAvatar, userAvatarColor, id, timeStamp)

                    MessageService.messages.add(newMessage)
                    updateMessages()
                }
            }
        }
    }

    private fun updateMessages() {
        messageAdapter.notifyDataSetChanged()
        if (messageAdapter.itemCount > 0) {
            messageListView.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }

    }

}
