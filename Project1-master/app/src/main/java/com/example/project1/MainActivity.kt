package com.example.project1

/*  This activity serves as the main menu screen. Users can select to create/host a game as a JUDGE (instructor)
    or to join a game as a DEBATOR (student).
 */

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO: Initiate HARDCODED database here- WILL FILL WITH DATA IF EMPTY
        GlobalScope.launch{
            DatabaseClasses.AppDatabase.getDB(baseContext)
        }

        //go to hosting- becomes JUDGE
        hostBtn.setOnClickListener{
            startActivity(Intent(this, HostNewGame::class.java))
        }
        //go to game list- becomes DEBATOR
        joinBtn.setOnClickListener{
            startActivity(Intent(this, SessionJoinListActivity::class.java))
        }

        //quit game
        exitBtn.setOnClickListener{
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ -> super@MainActivity.onBackPressed() })
                .setNegativeButton("No", null)
                .show()
        }
    }

    //on returning to this activity, remove prefs
    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        prefs.edit().remove(resources.getString(R.string.PrefKeyHostName)).apply()
        prefs.edit().remove(resources.getString(R.string.PrefKeyPlayerName)).apply()
        prefs.edit().remove(resources.getString(R.string.PrefKeyMainClaim)).apply()
    }
}