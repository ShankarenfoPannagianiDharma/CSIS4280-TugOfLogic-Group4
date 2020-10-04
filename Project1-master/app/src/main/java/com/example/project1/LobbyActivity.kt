package com.example.project1

/*  this screen is the LOBBY of a game session. HOSTS stay here after they created a game, and DEBATORS will
    join from their end. The HOST/JUDGE may start the game proper at any time.

    TODO: Distinguish UI between JUDGE/DEBATORS: Debators CANNOT do anything here- no buttons!
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_lobby_waiting.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LobbyActivity : AppCompatActivity() {
    private var host : Boolean = false      //is user HOST/PLAYER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_waiting)

        //!!! DISTINGUISH BETWEEN JUDGE AND DEBATOR !!! - via sharedPreferences
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        if(prefs.getString(getString(R.string.PrefKeyHostName),null) != null)
        { host = true }
        else{   //a debator has entered: Insert username into the database
            GlobalScope.launch {
                val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                //create debator object to be inserted into DB
                val thisDebator = DatabaseClasses.Debator(0, prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!, position = false, switched = false)
                db.debatorDAO().insertDebator(thisDebator)
            }
        }

        val job = GlobalScope.launch {
            //acquire ArrayOf users in database!
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            db.debatorDAO().getAllNames().collect {
                // access the listView from xml file
                val arrayAdapter: ArrayAdapter<*>
                arrayAdapter = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                listWait.adapter = arrayAdapter
            }


        }

        //TODO: kick a player out after selecting them !!RESTRICT ACCESS TO ADMIN ONLY!!
        if(host){
            listWait.setOnItemClickListener { parent, view, position, id ->
                val element = parent.getItemAtPosition(position) // The item that was clicked
                //remove from database, remove from list, boot them out.
            }
        }

        //Display appropriate controls for JUDGE OR DEBATORS. JUDGE can start game & abort the game. DEBATORS can only quit.
        if(!host){
            Lobby_Player_Quit_btn.visibility = View.VISIBLE
            Lobby_Host_Abort_btn.visibility = View.GONE
            //TODO:Host begin button should not be visible- but for testing, this is hardcoded to ok
            //Lobby_Host_Begin_btn.visibility = View.GONE
        }

        //start the game proper TODO:SEND JUDGE TO HUB, but SEND DEBATORS TO SIDE PICKING. This is not necessary in final product since only judge can press. Debators can only wait until this is pressed, then get sent to their side picking
        Lobby_Host_Begin_btn.setOnClickListener{
            if(host) {
                //is HOST/JUDGE -> wait in hub for players to make statements
                startActivity(Intent(this, HubJudgeActivity::class.java))
            }
            else {
                //is PLAYER/DEBATOR -> must pick sides AND create statements
                job.cancel()
                startActivity(Intent(this, ForOrAgainst::class.java))
            }
        }

        //abort game for host TODO: MUST END SESSION: KICK ALL PLAYERS!
        Lobby_Host_Abort_btn.setOnClickListener{
            finish();
        }
    }
}