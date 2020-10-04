package com.example.project1

/*  This is the bout round interface for DEBATORS.
    TODO: the chat with all users must enable discussion
    DEBATORS must be able to vote on the RiP in the bout: Yes or No.
    TODO: Interleave the bouts- For,Against,For,Against, so on and so forth
    TODO: This voting system needs to be broadcasted, but calculated only ONCE.
    TODO: Extra features: people not yet voted count
    the timer in the bout counts down and will finish the bout automatically.
    TODO:    !!  STILL A PROTOTYPE: VOTING DOES NOT YET WORK  !!
 */

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_bout_debator.*
import kotlinx.android.synthetic.main.activity_hub.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BoutDebatorActivity : AppCompatActivity() {
    lateinit var boutContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bout_debator)

        //set mc
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        bout_debator_mc_lbl.text = mc

        //get the bout data
        boutContent = intent.getStringExtra(getString(R.string.BoutIntentKey))!!
        bout_debator_statement_txt.text = boutContent

        //set if debator can change statement
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            val debSide = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!!NONAME")!!).position
            val ripSide = db.statementDAO().findByText(boutContent).position
            //only can change RiP if on the same side
            if(debSide == ripSide) {
                runOnUiThread{ bout_debator_edit_statement.visibility = View.VISIBLE }
            }

        }

        //edit RiP button
        bout_debator_edit_statement.setOnClickListener {
            //create a text dialogbox for new RiP
            val inputDialog  = AlertDialog.Builder(this@BoutDebatorActivity)
            val input = EditText(this@BoutDebatorActivity)
            input.setText(boutContent)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            input.layoutParams = lp
            inputDialog.setView(input);
            //ok button on dialog: check statement not empty, insert into database.
            inputDialog.setPositiveButton("Accept", DialogInterface.OnClickListener() { _, _ ->
                //statement is not empty
                val textContent = input.text.toString()
                if(!textContent.isNullOrEmpty()){
                    //insert into database
                    GlobalScope.launch {
                        val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                        val oldStatement = db.statementDAO().findByText(boutContent)
                        val newStatement = DatabaseClasses.Statement(oldStatement.id,textContent,oldStatement.position,oldStatement.status)
                        db.statementDAO().updateStatement(newStatement)
                        Log.i("InitStatemnets","STATEMENT UPDATED")
                    }
                }
            })//end inputdialog onConfirm
            inputDialog.show()
        }//end edit button

        //TODO: make the chat function

        //agree button
        bout_debator_vote_yes.setOnClickListener {
            //TODO: this vote would broadcast this debator as yes, to be collected by admin and calculated when game ends
        }
        //disagree button
        bout_debator_vote_no.setOnClickListener {
            //TODO: this vote would broadcast this debator as no, to be collected by admin and calculated when game ends
        }

        //set timer
        boutTimer.start()

    }

    //countdown for waiting bouts: create new object from abstract of CDT (10 secs)
    val boutTimer = object : CountDownTimer(10000, 1000) {
        //on calculate votes and return to hub
        override fun onFinish() {
            finish();
        }

        //updates UI
        override fun onTick(timeLeft: Long) {
            //find minutes & seconds (convert to seconds -> / || %)
            val minute = (timeLeft / 1000) / 60
            val seconds = (timeLeft / 1000) % 60
            bout_debator_timer_txt.text = "$minute:$seconds"
        }
    }
}