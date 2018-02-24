package com.dragomirproychev.smack.Services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.dragomirproychev.smack.Controller.App
import com.dragomirproychev.smack.Model.Channel
import com.dragomirproychev.smack.Utilities.URL_GET_CHANNELS
import org.json.JSONException

object MessageService {

    val channels = ArrayList<Channel>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null,
                Response.Listener { response ->
                    try {
                        channels.clear()
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
}