package com.example.project1

//  Contributors: Kaur, John, Shanka

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.android.synthetic.main.activity_hub_judge.*
import kotlinx.android.synthetic.main.activity_lobby_waiting.*
import kotlinx.android.synthetic.main.activity_session_join_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SessionJoinListActivity : AppCompatActivity() {
    lateinit var jsonLobbies : List<DataLobby>

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
        GlobalScope.launch {

            //get lobbies from AWS database
            var lobbiesString: String = ""
            try{
                //get url data from assets
                val urlJSONString: String = applicationContext.assets.open("SiteData.json").bufferedReader().use { it.readText() }
                //parse JSON with Moshi
                val moshi : Moshi = Moshi.Builder().build()
                val type = com.squareup.moshi.Types.newParameterizedType(DataUrl::class.java)
                val moshiAdapter: JsonAdapter<DataUrl> = moshi.adapter(type)
                val urlObject = moshiAdapter.fromJson(urlJSONString)!!

                //set content-type application/json
                val url = URL("http:/" + urlObject.server + ":" + urlObject.port + "/GetSessions")
                Log.i("GetSessions", url.toString())
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.readTimeout = 15*1000
                conn.connect() //<-- ACTUALLY CONNECT


                // read the output from the server
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                lobbiesString = reader.readLines().toString()
                reader.close()

                //take off extra brackets
                lobbiesString = lobbiesString.replace("[[","[")
                lobbiesString = lobbiesString.replace("]]","]")

                Log.i("GetSessions", "Done:$lobbiesString")
            } catch (e : Exception){
                e.printStackTrace()
                Log.i("GetSessions","Failed.")
            }

            //convert raw string into object
            val moshi : Moshi = Moshi.Builder().build()
            val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, DataLobby::class.java)
            val moshiAdapter: JsonAdapter<List<DataLobby>> = moshi.adapter(type)
            jsonLobbies = moshiAdapter.fromJson(lobbiesString)!!

            // access the listView from xml file
            var cListView = findViewById<ListView>(R.id.WaitList)
            val arrayAdapter = SessionAdapter(baseContext,jsonLobbies)
            runOnUiThread {
                cListView.adapter = arrayAdapter
            }
        }

        //when item is clicked, proceed to go into THAT session's lobby & get the mainclaim
        WaitList.setOnItemClickListener { _, _, position, _  ->
            val targetLobby = jsonLobbies[position]
            Log.i("DBTR SessnJnLstItm",targetLobby.MainClaim);

            //get the mainclaim for THAT lobby
            val prefs = getSharedPreferences(getString(R.string.PrefName),Context.MODE_PRIVATE)
            prefs.edit().putString(getString(R.string.PrefKeyMainClaim),targetLobby.MainClaim).apply()

            //proceed to that game's lobby as DEBATOR
            val intent = Intent(this, LobbyActivity::class.java)
            startActivity(intent)
        }

        //quit button
        SessionListQuitGameBtn.setOnClickListener { finish() }
    }

    //custom adapter for session listview
    class SessionAdapter(private val context: Context, private val dataSource: List<DataLobby>) : BaseAdapter(){
        private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(position: Int, p1: View?, p2: ViewGroup?): View {
            val rowView = inflater.inflate(R.layout.listitem_sessions, p2, false)
            val MCTextView = rowView.findViewById(R.id.li_Session_MC_txt) as TextView
            val JudgeTextView = rowView.findViewById(R.id.li_Session_Judge_txt) as TextView

            MCTextView.text = dataSource[position].MainClaim
            JudgeTextView.text = dataSource[position].Judge

            return rowView
        }

        override fun getItem(p0: Int): Any {
            return dataSource[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return dataSource.size
        }
    }
}