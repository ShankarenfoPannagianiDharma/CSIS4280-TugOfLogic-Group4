package com.example.project1

/*  This is the final activity of the game. the game result is calculated previously, and can be displayed here.
    DEBATORS that switched must now cite their reason why- through a dialogbox.
    ending the button returns to mainactivity.
 */

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_end_cite.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class EndCiteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_cite)

        //set ui mainclaim - winner
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        val winner = intent.getIntExtra(getString(R.string.GameResultIntentKey),0)
        var heading = mc
        when(winner){
            0 -> heading += " is very controversial. No conclusion can be made..."
            1 -> heading += " is true."
            2 -> heading += " is false."
        }
        end_cite_winner_lbl.text = heading

        //ask switched players for citations
        if(prefs.getString(getString(R.string.PrefKeyPlayerName),null) != null) {
            GlobalScope.launch {
                val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                val debatorName = prefs.getString(getString(R.string.PrefKeyPlayerName),null)!!
                Log.i("EndCite Switched",db.debatorDAO().isDebatorSwitched(debatorName).toString())
                if(db.debatorDAO().isDebatorSwitched(debatorName)){
                    //if switched, dialogbox
                    val inputDialog  = AlertDialog.Builder(this@EndCiteActivity)
                    val input = EditText(this@EndCiteActivity)
                    val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
                    input.layoutParams = lp
                    inputDialog.setView(input)
                    //ok button on dialog: check statement not empty, insert into database.
                    inputDialog.setPositiveButton("Accept", DialogInterface.OnClickListener() { _, _ ->
                        //statement is not empty
                        val textContent = input.text.toString()
                        if(!textContent.isNullOrEmpty()) {
                            //put to list
                            GlobalScope.launch {
                                //connect db
                                val db = DatabaseClasses.AppDatabase.getDB(baseContext)

                                db.citeDAO().getAllCites().collect {
                                    val arrayAdapter = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                                    runOnUiThread {
                                        end_cite_list.adapter = arrayAdapter
                                    }
                                }
                            }
                        }
                    })//end inputdialog ok
                    inputDialog.show()
                }//end if user has switched
            }
        }//end if user is debator

        //quit button->main activity
        end_cite_end_btn.setOnClickListener {
            //clear out databases
            GlobalScope.launch {
                val db = DatabaseClasses.AppDatabase.getDB(baseContext)
                db.clearAllTables()
            }

            //clear out preferences
            val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
            prefs.edit().remove(resources.getString(R.string.PrefKeyHostName)).apply()
            prefs.edit().remove(resources.getString(R.string.PrefKeyPlayerName)).apply()
            prefs.edit().remove(resources.getString(R.string.PrefKeyMainClaim)).apply()

            startActivity(Intent(this@EndCiteActivity, MainActivity::class.java))
        }
    }
}