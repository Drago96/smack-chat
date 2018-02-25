package com.dragomirproychev.smack.Services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.dragomirproychev.smack.Controller.App
import com.dragomirproychev.smack.Model.Channel
import com.dragomirproychev.smack.Model.Message
import com.dragomirproychev.smack.Utilities.URL_GET_CHANNELS
import com.dragomirproychev.smack.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null,
                Response.Listener { response ->
                    try {
                        clearChannels()
                        for(i in 0 until response.length()){
                            val channel = response.getJSONObject(i)

                            val name = channel.getString("name")
                            val description = channel.getString("description")
                            val id = channel.getString("_id")

                            val newChannel = Channel(name,description,id)

                            channels.add(newChannel)

                        }
                        complete(true)

                    } catch(e: JSONException){
                        Log.d("JSON","EXC: ${e.localizedMessage}")
                        complete(false)
                    }

                }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.prefs.authToken}"
                return headers
            }
        }

        App.prefs.requestQueue.add(channelsRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit){

        val url = "$URL_GET_MESSAGES$channelId"

        val messageRequest = object : JsonArrayRequest(Method.GET,url,null, Response.Listener {response ->
            try {

                clearMessages()
                for(x in 0 until response.length()){

                    val message = response.getJSONObject(x)
                    val messageBody = message.getString("messageBody")
                    val channelId = message.getString("channelId")
                    val id = message.getString("_id")
                    val userName = message.getString("userName")
                    val avatar = message.getString("userAvatar")
                    val userAvatarColor = message.getString("userAvatarColor")
                    val timeStamp = message.getString("timeStamp")

                    val newMessage = Message(messageBody, userName, channelId, avatar, userAvatarColor,id,timeStamp)
                    MessageService.messages.add(newMessage)
                }
                complete(true)
            } catch (e: JSONException){
                Log.d("JSON","EXC: ${e.localizedMessage}")
                complete(false)
            }

        }, Response.ErrorListener {
            Log.d("ERROR", "Could not retrieve messages")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String,String>()
                headers["Authorization"] = "Bearer ${App.prefs.authToken}"
                return headers
            }
        }

        App.prefs.requestQueue.add(messageRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChannels(){
        channels.clear()
    }
}