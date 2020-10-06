package com.example.project1

//  Contributors: John, Kaur, Shanka

/*  This is the final voting activity- for DEBATORS to VOTE on the MAIN CLAIM.
    DEBATORS can either vote yes or no. Their initial voting position is based on their stance initially.
    timer counts down to game end, then game finishes: proceed to results & citation

    JUDGE also goes here. They can only witness the
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import kotlinx.android.synthetic.main.activity_final_mc_voting.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FinalMCVotingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_final_mc_voting)

        //set main claim
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        final_vote_mc_lbl.text = mc

        //set interfaces if JUDGE
        if(prefs.getString(getString(R.string.PrefKeyHostName),null) != null) {
            final_vote_end.visibility = View.VISIBLE
            final_vote_against_txt.visibility = View.VISIBLE
            final_vote_for_txt.visibility = View.VISIBLE
            final_vote_against_btn.visibility = View.GONE
            final_vote_for_btn.visibility = View.GONE
            final_vote_position_lbl.visibility = View.GONE
            final_vote_against_num_txt.visibility = View.VISIBLE
            final_vote_for_num_txt.visibility = View.VISIBLE

            //fill number of votes TODO:MUST BE LIVEDATA/FLOW
            GlobalScope.launch {
                val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                val forCount = db.debatorDAO().getNumSide(true)
                val againstCount = db.debatorDAO().getNumSide(false)
                runOnUiThread {
                    final_vote_against_num_txt.text = againstCount.toString()
                    final_vote_for_num_txt.text = forCount.toString()
                }
            }

            //JUDGE premature end button
            final_vote_end.setOnClickListener {
                endVoting()
            }

        } else {    //interfaces if DEBATOR
            //set position
            uiDebatorPosUpdate()

            //FOR button
            final_vote_for_btn.setOnClickListener {
                //check current position. do nothing if match, else switch update db and ui.
                var position = false
                GlobalScope.launch {
                    val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                    val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                    position = db.debatorDAO().getDebatorSide(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!)
                    if(!position){
                        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                        val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                        //get debator object to update into DB
                        val debator = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!)
                        val updateDebator = DatabaseClasses.Debator(debator.id, prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!, position = true, switched = true)
                        db.debatorDAO().updateDebator(updateDebator)
                        uiDebatorPosUpdate()
                    }
                }
            }

            //AGAINST button
            final_vote_against_btn.setOnClickListener {
                //check current position. do nothing if match, else switch update db and ui.
                var position = false
                GlobalScope.launch {
                    val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                    val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                    position = db.debatorDAO().getDebatorSide(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!)
                    if(position){
                        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                        val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                        //get debator object to update into DB
                        val debator = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!)
                        val updateDebator = DatabaseClasses.Debator(debator.id, prefs.getString(getString(R.string.PrefKeyPlayerName), "ERROR!NONAME")!!, position = false, switched = true)
                        db.debatorDAO().updateDebator(updateDebator)
                        uiDebatorPosUpdate()
                    }
                }
            }
        }

        //set timer
        finalVoteTime.start()
    }

    //countdown for waiting bouts: create new object from abstract of CDT (30 secs)
    val finalVoteTime = object : CountDownTimer(30000, 1000) {
        //on finish, go to end activity
        override fun onFinish() {
            endVoting()
        }

        //updates UI
        override fun onTick(timeLeft: Long) {
            //find minutes & seconds (convert to seconds -> / || %)
            val minute = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            final_vote_timer_lbl.text = "$minute:$seconds"
        }
    }

    //finish voting. Proceed to results & citation
    private fun endVoting(){
        //calculate winner
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            val forCount = db.debatorDAO().getNumSide(true)
            val againstCount = db.debatorDAO().getNumSide(false)
            val finishIntent = Intent(this@FinalMCVotingActivity, EndCiteActivity::class.java)
            when {
                forCount > againstCount -> {
                    finishIntent.putExtra(getString(R.string.GameResultIntentKey), 1)
                    startActivity(finishIntent)
                }
                forCount < againstCount -> {
                    finishIntent.putExtra(getString(R.string.GameResultIntentKey), 2)
                    startActivity(finishIntent)
                }
                forCount == againstCount -> {
                    finishIntent.putExtra(getString(R.string.GameResultIntentKey), 0)
                    startActivity(finishIntent)
                }
            }//end decision making
        }
    }

    //make the UI updated to ensure the debator's position
    private fun uiDebatorPosUpdate(){
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
            val position = db.debatorDAO().getDebatorSide(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!)
            runOnUiThread{
                when(position){
                    true -> final_vote_position_lbl.text = "You agree with the claim."
                    false -> final_vote_position_lbl.text = "You disagree with the claim"
                }
            }
        }

    }

}