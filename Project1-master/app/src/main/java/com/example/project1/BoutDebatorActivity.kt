package com.example.project1

// Contributors: Shanka, John, Kaur

/*  This is the bout round interface for DEBATORS.
    TODO: the chat with all users must enable discussion
    DEBATORS must be able to vote on the RiP in the bout: Yes or No.
    TODO: Interleave the bouts- For,Against,For,Against, so on and so forth
    TODO: This voting system needs to be broadcasted, but calculated only ONCE.
    TODO: Extra features: people not yet voted count
    the timer in the bout counts down and will finish the bout automatically.
 */

import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_bout_debator.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class BoutDebatorActivity : AppCompatActivity() {
    lateinit var boutContent: String
    lateinit var currentDebator: DatabaseClasses.Debator //DataDebator

    //broadcast to receive notification of new chat data->connect to AWS
    private lateinit var br: BroadcastReceiver
    //intent filter specific for chat msg
    private val filter = IntentFilter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bout_debator)

        br = ChatBroadCastReceiver(baseContext)

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
            currentDebator = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!!NONAME")!!)
            val debSide = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!!NONAME")!!).position
            val ripSide = db.statementDAO().findByText(boutContent).position
            //only can change RiP if on the same side
            if(debSide == ripSide) {
                runOnUiThread{ bout_debator_edit_statement.visibility = View.VISIBLE }
            }
        }

        //clear data in chat table
        GlobalScope.launch{
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
                    bout_debator_chatList.layoutManager = LinearLayoutManager(baseContext,LinearLayoutManager.VERTICAL,false)
                    bout_debator_chatList.adapter = adapter
                }
                Log.i("CHATTING","Refreshed adapter")
            }
        }

        //register broadcastreceiver
        filter.addAction(packageName + "CHATMESSAGE");
        registerReceiver(br,filter)

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
                        Log.i("BoutDebator","STATEMENT UPDATED")
                    }
                }
            })//end inputdialog onConfirm
            inputDialog.show()
        }//end edit button

        //keep listening to server data
        /*
        *   Method 1: Chats work by using listeners.
        *   When a user sends a message to the server, the server receives the message and saves it in JSON.
        *   This new JSON is then sent to all users, thus giving them the new chat.
        *
        *   Android has to implement the listeners to receive server updates,
        *   and AWS Server has to keep resending updated data.
        *
        *   Method 2: notificating.
        *   When message is sent to AWS, android app will also make all other users reconnect and re-request data.
        *
        *   Method 3: Broadcasting & Room.
        *   Chats are sent with broadcast intents- intents have chat string and faction attached, which are broadcasted.
        *   Broadcast listeners implemented by other users in app will receive this broadcast- and unpack chat data.
        *   This data is then added to the local room, updating the listview.
        *   ----USING METHOD 3----
        * */

        //send chat to broadcast
        bout_debator_chat_submit_btn.setOnClickListener {
            //get chatbox input data
            val chatTV = bout_debator_chat_enter_txt;
            val debatorMessage = chatTV.text.toString();
            var position : Int = 0
            if(!debatorMessage.isNullOrEmpty()) {
                //create broadcast intent
                val intent = Intent()
                intent.action = packageName + "CHATMESSAGE"
                //put data into intent
                intent.putExtra("CHATMSG",debatorMessage)
                intent.putExtra("SENDER",currentDebator.username)
                position = if(currentDebator.position){
                    1
                } else{
                    0
                }
                intent.putExtra("POSITION", position)
                //send broadcast
                sendBroadcast(intent)

                //gonnect to AWS, put text into json
                /*/put data into AWS server
                GlobalScope.launch {
                    try {
                        //get url data from assets
                        val urlJSONString: String =
                            applicationContext.assets.open("SiteData.json").bufferedReader()
                                .use { it.readText() }
                        //parse JSON with Moshi
                        val moshi: Moshi = Moshi.Builder().build()
                        val type = com.squareup.moshi.Types.newParameterizedType(DataUrl::class.java)
                        val moshiAdapter: JsonAdapter<DataUrl> = moshi.adapter(type)
                        val urlObject = moshiAdapter.fromJson(urlJSONString)!!

                        //set content-type application/json
                        val url =
                            URL("http:/" + urlObject.server + ":" + urlObject.port + "/NewChat")
                        Log.i("CreateSession", url.toString())
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.setRequestProperty(
                            "Content-type",
                            "application/json"
                        ) //<-- content-type as defined in server python
                        conn.readTimeout = 15 * 1000
                        conn.doOutput = true
                        conn.connect() //<-- ACTUALLY CONNECT

                        val jsonToSend = JSONObject()
                        jsonToSend.put("Username", currentDebator.username)
                        jsonToSend.put("Position", currentDebator.position)
                        jsonToSend.put("Message", debatorMessage)
                        Log.i("Chatting", "Writing: $jsonToSend")

                        val outputStream = DataOutputStream(conn.outputStream);
                        outputStream.writeBytes(jsonToSend.toString())              //<-- Put the json data in POST content
                        outputStream.flush()
                        outputStream.close()

                        // read the output from the server
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        reader.close()

                        Log.i("Chatting", "Done.")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.i("Chatting", "Failed.")
                    }
                }*/
            }
            chatTV.setText("")
        }

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
            bout_debator_timer_txt.text = "$minute:$seconds"
        }
    }

    //onStop, stop broadcast receiver
    override fun onStop(): Unit {
        super.onStop()
        unregisterReceiver(br)
    }
}

