package com.example.project1

/*  This is the judge's perspective of bouts. They can TODO: Chat with debators.
    And end the bout prematurely.
 */

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import kotlinx.android.synthetic.main.activity_bout_judge.*

class BoutJudgeActivity : AppCompatActivity() {
    lateinit var boutContent: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bout_judge)

        //set mainclaim
        //set mc
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        bout_judge_mc_lbl.text = mc

        //set statement
        boutContent = intent.getStringExtra(getString(R.string.BoutIntentKey))!!
        bout_judge_statement_txt.text = boutContent

        //force finish bout
        bout_judge_end_bout_btn.setOnClickListener {
            if(boutTimer != null)
            {boutTimer.cancel()}
            finishBout()
        }

        //set timer
        boutTimer.start()
    }

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
            bout_judge_timer_txt.text = "$minute:$seconds"
        }
    }

    private fun finishBout() {
        finish()
    }
}