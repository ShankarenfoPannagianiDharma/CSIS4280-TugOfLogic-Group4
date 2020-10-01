package com.example.project1

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_wait.*

class WaitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wait)
        //Rooms list
        val arrayAdapter1: ArrayAdapter<*>
        val room = arrayOf("Room1", "Poom2", "Room3")

        // access the listView from xml file
        var cListView = findViewById<ListView>(R.id.WaitList)
        arrayAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, room)
        cListView.adapter = arrayAdapter1

        WaitList.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) // The item that was clicked
            val intent = Intent(this, ClaimActivity::class.java)
            startActivity(intent)
        }


    }
}