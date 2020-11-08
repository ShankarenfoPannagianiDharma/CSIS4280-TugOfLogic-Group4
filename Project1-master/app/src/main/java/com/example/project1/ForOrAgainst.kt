package com.example.project1

//  Contributors: Shanka, John, Kaur

/*  This activity allows the DEBATORS to pick their initial side in the session.
    after a choice is made, they proceed to the claims activity to make statements on their end.
    THE CHOICE IS SAVED INTO THE DATABASE, so in the next activity, refer to dbase for which elements to display.
 */

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_for_or_against.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ForOrAgainst : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_for_or_against)

        //get the MAINCLAIM from Prefs
        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val mc = prefs.getString(getString(R.string.PrefKeyMainClaim), "")
        sidepick_MC_txt.text = mc

        //button functions for or against
        sidepick_Agree_btn.setOnClickListener {
            //proceed to statementmaker
            defineUser(true)
        }

        sidepick_Disagree_btn.setOnClickListener {
            //proceed to statementmaker
            defineUser(false)
        }
    }

    private fun defineUser(position : Boolean){
        //update database the user position info
        GlobalScope.launch {
            val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //get debator object to update into DB
            val idKey = db.debatorDAO().findByName(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!).id
            val updateDebator = DatabaseClasses.Debator(idKey, prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!, position = position, switched = false)

            db.debatorDAO().updateDebator(updateDebator)
        }

        //proceed to statements creation
        startActivity(Intent(this, InitialStatementsActivity::class.java))
    }
}