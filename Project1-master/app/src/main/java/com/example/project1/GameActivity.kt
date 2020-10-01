package com.example.project1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        //ListView code
        // use arrayadapter and define an array
        val arrayAdapter: ArrayAdapter<*>
        val users = arrayOf("Player1", "Player2", "Player3","Player4")

        // access the listView from xml file
        var mListView = findViewById<ListView>(R.id.listWait)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, users)
        mListView.adapter = arrayAdapter

        listWait.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) // The item that was clicked
            val intent = Intent(this, WaitActivity::class.java)
            startActivity(intent)
        }


        cl.setOnClickListener{
            startActivity(Intent(this, ClaimActivity::class.java))
        }
    }
}