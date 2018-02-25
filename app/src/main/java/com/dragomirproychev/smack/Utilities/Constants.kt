package com.dragomirproychev.smack.Utilities

const val BASE_URL = "https://smack-chat-android-app.herokuapp.com/v1/"
const val SOCKET_URL = "https://smack-chat-android-app.herokuapp.com/"

const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel"
const val URL_GET_MESSAGES = "${BASE_URL}message/byChannel/"

const val BROADCAST_USER_DATA_CHANGE = "BROADCAST_USER_DATA_CHANGE"

const val SOCKET_NEW_CHANNEL = "newChannel"
const val SOCKET_CHANNEL_CREATED = "channelCreated"
const val SOCKET_NEW_MESSAGE = "newMessage"
const val SOCKET_MESSAGE_CREATED = "messageCreated"

const val REQUEST_EXIT = 400
const val RESULT_FINISH = 200