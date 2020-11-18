package com.example.project1

//  Contributors: Kaur, John, Shanka

/*  This screen allows the HOST to define the MAIN CLAIM that players will have to debate on.
    The JUDGE will have to state their username. These two items will be saved in the sharedPreferences,
    since they will be referenced throughout the whole game session. Once both items are entered and legal,
    the game will initialize a database to be used for this specific session.

    !!IMPORTANT!!: The EXISTENCE of PrefHostName designates that the user IS the host! After game is complete,
    REMOVE ALL PREFERENCES to preserve the exclusivity of the roles!
 */

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_host_new_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HostNewGame : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host_new_game)

        //submit data- MuSt NOT BE EMPTY- and proceed to game lobby as judge
        HostingNew_Confirm_btn.setOnClickListener {
            //get inputted data
            val judgeName = HostingNew_HostName_txt.text.toString()
            val mainClaim = HostingNew_MainClaim_txt.text.toString()
            //verify neither empty
            if(judgeName.isNotBlank() && mainClaim.isNotBlank())
            {
                //save data in session
                val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
                prefs.edit().putString(getString(R.string.PrefKeyHostName),judgeName).apply()
                prefs.edit().putString(getString(R.string.PrefKeyMainClaim),mainClaim).apply()

                //put data into AWS server
                GlobalScope.launch {
                    try{
                        //get url data from assets
                        val urlJSONString: String = applicationContext.assets.open("SiteData.json").bufferedReader().use { it.readText() }
                        //parse JSON with Moshi
                        val moshi : Moshi = Moshi.Builder().build()
                        val type = com.squareup.moshi.Types.newParameterizedType(DataUrl::class.java)
                        val moshiAdapter: JsonAdapter<DataUrl> = moshi.adapter(type)
                        val urlObject = moshiAdapter.fromJson(urlJSONString)!!

                        //set content-type application/json
                        val url = URL("http:/" + urlObject.server + ":" + urlObject.port + "/SessionCreate")
                        Log.i("CreateSession", url.toString())
                        val conn = url.openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-type", "application/json") //<-- content-type as defined in server python
                        conn.readTimeout = 15*1000
                        conn.doOutput = true
                        conn.connect() //<-- ACTUALLY CONNECT

                        val jsonToSend = JSONObject()
                        jsonToSend.put("MainClaim", mainClaim)
                        jsonToSend.put("Judge", judgeName)
                        Log.i("CreateSession", "Writing: $jsonToSend")

                        val outputStream = DataOutputStream(conn.outputStream);
                        outputStream.writeBytes(jsonToSend.toString())              //<-- Put the json data in POST content
                        outputStream.flush()
                        outputStream.close()

                        // read the output from the server
                        val reader = BufferedReader(InputStreamReader(conn.inputStream))
                        reader.close()

                        Log.i("CreateSession","Done.")
                    } catch (e : Exception){
                        e.printStackTrace()
                        Log.i("SessionCreate","Failed.")
                    }
                }

                startActivity(Intent(this, LobbyActivity::class.java))
            }
        }


    }
}