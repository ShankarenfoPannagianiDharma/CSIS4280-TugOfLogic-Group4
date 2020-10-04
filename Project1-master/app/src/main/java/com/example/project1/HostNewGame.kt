package com.example.project1

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
import androidx.room.Room
import com.example.project1.DatabaseClasses.AppDatabase
import kotlinx.android.synthetic.main.activity_host_new_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

                startActivity(Intent(this, LobbyActivity::class.java))
            }
        }


    }
}