package com.example.project1

//  Contributors: Shanka

/*  This is the judge's perspective of bouts. They can TODO: Chat with debators.
    And end the bout prematurely.
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_bout_judge.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BoutJudgeActivity : AppCompatActivity() {
    lateinit var boutContent: String

    //broadcast to receive notification of new chat data->connect to AWS
    private lateinit var br: BroadcastReceiver
    //intent filter specific for chat msg
    private val filter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bout_judge)

        //set mainclaim
        //set mc
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val judgename = prefs.getString(getString(R.string.PrefKeyHostName),null)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        bout_judge_mc_lbl.text = mc

        //listen to local chat database live data
        //clear data in chat table
        GlobalScope.launch {
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            db.chatDAO().clearChats()
        }
        //get livedata of chat list
        GlobalScope.launch {
            //connect to database
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //fill chats from database
            db.chatDAO().getAllChats().collect {
                val adapter = ListAdapterChat(baseContext, it)
                runOnUiThread {
                    bout_judge_chatList.layoutManager = LinearLayoutManager(
                        baseContext,
                        LinearLayoutManager.VERTICAL, false
                    )
                    bout_judge_chatList.adapter = adapter
                }
                Log.i("CHATTING", "Refreshed adapter")
            }
        }
        //register broadcastlistener
        br = ChatBroadCastReceiver(baseContext)
        filter.addAction(packageName + "CHATMESSAGE");
        registerReceiver(br, filter)

        //set statement
        boutContent = intent.getStringExtra(getString(R.string.BoutIntentKey))!!
        bout_judge_statement_txt.text = boutContent

        //force finish bout
        bout_judge_end_bout_btn.setOnClickListener {
            boutTimer?.cancel()
            finishBout()
        }

        //send chat to broadcast
        bout_judge_chat_submit_btn.setOnClickListener {
            //get chatbox input data
            val chatTV = bout_judge_chat_enter_txt;
            val debatorMessage = chatTV.text.toString();
            var position: Int = 0
            if (!debatorMessage.isNullOrEmpty()) {
                //create broadcast intent
                val intent = Intent()
                intent.action = packageName + "CHATMESSAGE"
                //put data into intent
                intent.putExtra("CHATMSG", debatorMessage)
                intent.putExtra("SENDER", judgename)
                intent.putExtra("POSITION", 2)
                //send broadcast
                sendBroadcast(intent)
            }
        }

        //set timer
        boutTimer.start()
    }

    private val boutTimer = object : CountDownTimer(10000, 1000) {
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

    //onStop, stop broadcast receiver
    override fun onStop(): Unit {
        super.onStop()
        unregisterReceiver(br)
    }
}