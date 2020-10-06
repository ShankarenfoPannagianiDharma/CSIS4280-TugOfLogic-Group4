package com.example.project1

//  Contributors: Kaur, John, Shanka

/*  This is the judge's perspective of the hub. Judge can personally force start a bout with button.
    TODO: possible judge only controls? sidebars? drawers? ideas...
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_hub_judge.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HubJudgeActivity : AppCompatActivity() {

    var arrayAdapterTRUE: ArrayAdapter<*>? = null
    var arrayAdapterFALSE: ArrayAdapter<*>? = null
    var boutsNum = 0;
    var statements: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hub_judge)

        //set mainclaim
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        hub_judge_mainclaim_lbl.text = mc

        //get all statements
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //load all statements TODO: Make it FLOW/LIVE
            statements = db.statementDAO().getAllStatements()
        }

        //fill lists with statements
        GlobalScope.launch {
            //connect to database
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //fill statements from database
            db.statementDAO().getSideStatements(true).collect {
                arrayAdapterTRUE =
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                runOnUiThread {
                    hub_judge_statements_for_list.adapter = arrayAdapterTRUE
                }
            }
        }
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            db.statementDAO().getSideStatements(false).collect {
                arrayAdapterFALSE =
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                runOnUiThread {
                    hub_judge_statements_against_list.adapter = arrayAdapterFALSE
                }
            }
        }

        //force start next bout
        hub_judge_forcestartbout_btn.setOnClickListener{
            hubStandBy?.cancel()
            beginNextBout()
        }

        //timer
        hubStandBy.start()
    }

    //on returning, start the countdown again TODO:Ensure RETURN from BOUT ONLY! (Hint?=activity result)
    override fun onRestart() {
        super.onRestart()
        hubStandBy.start()
    }

    //countdown for waiting bouts: create new object from abstract of CDT (10 secs)
    private val hubStandBy = object : CountDownTimer(10000, 1000) {
        //on finish, start next bout
        override fun onFinish() {
            beginNextBout()
        }

        //updates UI
        override fun onTick(timeLeft: Long) {
            //find minutes & seconds (convert to seconds -> / || %)
            val minute = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            hub_judge_timeleft.text = "$minute:$seconds"
        }
    }

    //proceed to next bout
    private fun beginNextBout(){
        if(boutsNum < statements!!.size) {    //if there are still statements to do, proceed to bouts
            var nextBout = statements!!.get(boutsNum)
            boutsNum++
            //begin next bout
            val boutIntent = Intent(this@HubJudgeActivity, BoutJudgeActivity::class.java)
            boutIntent.putExtra(getString(R.string.BoutIntentKey),nextBout)
            startActivity(boutIntent)
        } else {   //no statements left. Go to final voting
            startActivity(Intent(this@HubJudgeActivity, FinalMCVotingActivity::class.java))
        }
    }
}