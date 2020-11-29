package com.example.project1

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatBroadCastReceiver(val baseContext : Context) : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        //on receive intent, get extras, add into local Room
        val msg = p1!!.extras!!["CHATMSG"].toString()
        val sndr = p1!!.extras!!["SENDER"].toString()
        val pos : Int = p1!!.extras!!["POSITION"] as Int
        //create a chat data object
        val chatItem = DatabaseClasses.Chat(message=msg,sender = sndr,position = pos)

        //insert object into local room
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            db.chatDAO().insertChat(chatItem)
            Log.i("ChatReceiver","CHAT ADDED")
        }

    }


}
