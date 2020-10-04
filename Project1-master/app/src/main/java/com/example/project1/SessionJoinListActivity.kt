package com.example.project1

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_lobby_waiting.*
import kotlinx.android.synthetic.main.activity_session_join_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SessionJoinListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_join_list)

        //alertdialog with edittext for input player name
        val debatorNameDialog = AlertDialog.Builder(this@SessionJoinListActivity);
        debatorNameDialog.setTitle("Username");
        debatorNameDialog.setMessage("Enter your name");
        val inputText = EditText(baseContext);
        val layoutParam = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        inputText.layoutParams = layoutParam;
        debatorNameDialog.setView(inputText);
        debatorNameDialog.setPositiveButton("Confirm") { _, _ ->
            //make sure user input name = not empty/blank
            if(inputText.text.toString().isNotBlank()) {
                //put THIS PLAYER's username into preferences.
                val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                prefs.edit().putString(getString(R.string.PrefKeyPlayerName),inputText.text.toString()).apply()
            }
        }
        debatorNameDialog.show()

        //Rooms list
        //TODO: get rooms from broadcast. IN THIS VERSION, ROOMS ARE A TABLE IN DB. REPLACE AS NEEDED LATER.
        GlobalScope.launch {
            // use arrayadapter and define an array
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            val arrayAdapter1: ArrayAdapter<*>
            val room = db.sessionDAO().getRooms()

            // access the listView from xml file
            var cListView = findViewById<ListView>(R.id.WaitList)
            arrayAdapter1 = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, room)
            cListView.adapter = arrayAdapter1
        }

        //when item is clicked, proceed to go into THAT session's lobby & get the mainclaim
        WaitList.setOnItemClickListener { parent, view, position, id ->
            val element : String = parent.getItemAtPosition(position) as String // The item (STRING) that was clicked
            Log.i("DBTR SessnJnLstItm",element);

            //get the mainclaim for THAT lobby
            val prefs = getSharedPreferences(getString(R.string.PrefName),Context.MODE_PRIVATE)
            prefs.edit().putString(getString(R.string.PrefKeyMainClaim),element).apply()

            //proceed to that game's lobby as DEBATOR
            val intent = Intent(this, LobbyActivity::class.java)
            startActivity(intent)
        }

        //quit button
        SessionListQuitGameBtn.setOnClickListener { finish() }
    }
}