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
import android.util.Log
import android.widget.ArrayAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_hub.*
import kotlinx.android.synthetic.main.activity_hub_judge.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HubJudgeActivity : AppCompatActivity() {

    var arrayAdapterTRUE: ArrayAdapter<*>? = null
    var arrayAdapterFALSE: ArrayAdapter<*>? = null
    var boutsNum = 0;
    var statements: MutableList<String> = ArrayList()
    var statementsFor: MutableList<String> = ArrayList()
    var statementsAgainst: MutableList<String> = ArrayList()
    var statementsObj: List<StatementObj>? = null

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
            //get statements from AWS database
            var statementString: String = ""
            try{
                //get url data from assets
                val urlJSONString: String = applicationContext.assets.open("SiteData.json").bufferedReader().use { it.readText() }
                //parse JSON with Moshi
                val moshi : Moshi = Moshi.Builder().build()
                val type = com.squareup.moshi.Types.newParameterizedType(DataUrl::class.java)
                val moshiAdapter: JsonAdapter<DataUrl> = moshi.adapter(type)
                val urlObject = moshiAdapter.fromJson(urlJSONString)!!

                //set content-type application/json
                val url = URL("http:/" + urlObject.server + ":" + urlObject.port + "/GetStatements")
                Log.i("GetStatements", url.toString())
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.readTimeout = 15*1000
                conn.connect() //<-- ACTUALLY CONNECT


                // read the output from the server
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                statementString = reader.readLines().toString()
                reader.close()

                //take off extra brackets
                statementString = statementString.replace("[[","[")
                statementString = statementString.replace("]]","]")

                Log.i("GetStatements", "Done:$statementString")
            } catch (e : Exception){
                e.printStackTrace()
                Log.i("GetStatements","Failed.")
            }

            //convert raw statement string into object
            val moshi : Moshi = Moshi.Builder().build()
            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, StatementObj::class.java)
            val moshiAdapter: JsonAdapter<List<StatementObj>> = moshi.adapter(type)
            statementsObj = moshiAdapter.fromJson(statementString)!!
            for (s in statementsObj!!){
                statements.add(s.content)
                if(s.position.compareTo("true") == 0){
                    statementsFor.add(s.content)
                }else{
                    statementsAgainst.add(s.content)
                }

            }
            Log.i("HUBJUDGEACTIVITY","STATEMENTS LOADED: "+statements!!.size)

            arrayAdapterTRUE =
                statementsFor?.let {
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it) }
            runOnUiThread {
                    hub_judge_statements_for_list.adapter = arrayAdapterTRUE
            }

            arrayAdapterFALSE =
                statementsAgainst?.let {
                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it) }
            runOnUiThread {
                hub_judge_statements_against_list.adapter = arrayAdapterFALSE
            }
        }

        //fill lists with statements
//        GlobalScope.launch {
//            //connect to database
//            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
//            //fill statements from database
//            db.statementDAO().getSideStatements(true).collect {
//                arrayAdapterTRUE =
//                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
//                runOnUiThread {
//                    hub_judge_statements_for_list.adapter = arrayAdapterTRUE
//                }
//            }
//        }
//        GlobalScope.launch {
//            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
//            db.statementDAO().getSideStatements(false).collect {
//                arrayAdapterFALSE =
//                    ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
//                runOnUiThread {
//                    hub_judge_statements_against_list.adapter = arrayAdapterFALSE
//                }
//            }
//        }

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