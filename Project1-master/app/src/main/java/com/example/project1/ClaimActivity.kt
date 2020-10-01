package com.example.project1

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import kotlinx.android.synthetic.main.activity_claim.*
import kotlinx.android.synthetic.main.activity_wait.*

class ClaimActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claim)

        //Rooms list
        val arrayAdapter2: ArrayAdapter<*>
        val claim = arrayOf("Statement1", "Statement2", "Statement3")

        // access the listView from xml file
        var c1ListView = findViewById<ListView>(R.id.list_wait)
        arrayAdapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, claim)
        c1ListView.adapter = arrayAdapter2

        list_wait.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) // The item that was clicked
            val intent = Intent(this, StatementActivity::class.java)
            startActivity(intent)
        }
        //Start Game
        stGame.setOnClickListener{
            startActivity(Intent(this, StatementActivity::class.java))
        }

        //End Game
        endGame.setOnClickListener{
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id -> super@ClaimActivity.onBackPressed() })
                .setNegativeButton("No", null)
                .show()
        }

    }
}