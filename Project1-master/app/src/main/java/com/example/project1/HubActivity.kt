package com.example.project1

/*  This activity acts as a hub for DEBATORS/PLAYERS. They are able to see all statements & their status.
    They can switch sides on the mainclaim here, at any time.
    Further functions can be: a drawer for group chatting, add new/delete RiPs (by vote).

    TODO: make each statement distinguishable by state
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_hub.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HubActivity : AppCompatActivity() {

    var arrayAdapterTRUE: ArrayAdapter<*>? = null
    var arrayAdapterFALSE: ArrayAdapter<*>? = null
    var boutsNum = 0;
    var statements: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub)

        GlobalScope.launch {
            //connect to database
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //fill statements from database
            db.statementDAO().getSideStatements(true).collect {
                arrayAdapterTRUE =
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                runOnUiThread {
                    hub_statements_for_list.adapter = arrayAdapterTRUE
                }
            }
        }
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            db.statementDAO().getSideStatements(false).collect {
                arrayAdapterFALSE =
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                runOnUiThread {
                    hub_statements_against_list.adapter = arrayAdapterFALSE
                }
            }
        }

        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //load all statements TODO: Make it FLOW/LIVE
            statements = db.statementDAO().getAllStatements()
            Log.i("HUBACTIVITY","STATEMENTS LOADED: "+statements!!.size)

            //determine which side the player is on
            val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
            val position = db.debatorDAO().getDebatorSide(prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!)
            runOnUiThread { hub_player_status.text = if (position) { "You agree." } else { "You disagree." } }
        }

        //put main claim at the top
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        hub_mainclaim_lbl.text = mc

        //put character status
        GlobalScope.launch{
            val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //get debator object to update into DB
            val debator = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!)
            runOnUiThread { hub_player_status.text = if (debator.position) { "For" } else { "Against" } }
        }

        //switch button function
        hub_player_switchsides.setOnClickListener {
            GlobalScope.launch {
                Log.i("HUBswitch","Switching position")
                val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                //get debator object to update into DB
                val debator = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!)
                val updateDebator = DatabaseClasses.Debator(debator.id, prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!, position = !debator.position, switched = true)
                db.debatorDAO().updateDebator(updateDebator)
                runOnUiThread { hub_player_status.text = if (updateDebator.position) { "You agree." } else { "You disagree." } }
            }
        }//end switch sides button

        //start timer of hub
        if(hubStandBy != null) { hubStandBy!!.cancel() }
        hubStandBy.start()

    }//end oncreate

    //on returning, start the countdown again TODO:Ensure RETURN from BOUT ONLY! (Hint?=activity result)
    override fun onRestart() {
        super.onRestart()
        hubStandBy.start()
    }

    //countdown for waiting bouts: create new object from abstract of CDT (10 secs)
    private val hubStandBy = object : CountDownTimer(10000, 1000) {
        //on finish, start next bout
        override fun onFinish() {
            if(boutsNum < statements!!.size) {    //if there are still statements to do, proceed to bouts
                var nextBout = statements!!.get(boutsNum)
                boutsNum++
                //begin next bout
                val boutIntent = Intent(this@HubActivity, BoutDebatorActivity::class.java)
                boutIntent.putExtra(getString(R.string.BoutIntentKey),nextBout)
                startActivity(boutIntent)
            } else {   //no statements left. Go to final voting
                startActivity(Intent(this@HubActivity, FinalMCVotingActivity::class.java))
            }
        }

        //updates UI
        override fun onTick(timeLeft: Long) {
            //find minutes & seconds (convert to seconds -> / || %)
            val minute = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            hub_timeleft.text = "$minute:$seconds"
        }
    }
}