package com.example.project1

//  Contributors: Shanka

/*  Initial statements activity is the place after picking sides, DEBATORS must make statements regarding
    their position on the main claim. (Everyone can make one,) (statements can be deleted after voting,)
    after everyone is satisfied, they may proceed to the hub.
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
import kotlinx.android.synthetic.main.activity_initial_statements.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InitialStatementsActivity : AppCompatActivity() {
    var position = true       //the player's initial for-against position
    var arrayAdapter: ArrayAdapter<*>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_statements)


        val prefs = getSharedPreferences(getString(R.string.PrefName), Context.MODE_PRIVATE)
        val updateJob = GlobalScope.launch {
            //connect statements with table
            val db = DatabaseClasses.AppDatabase.getDB(baseContext)
            //get which side is relevant
            position = db.debatorDAO().getDebatorSide(prefs.getString(getString(R.string.PrefKeyPlayerName),"ERROR!NONAME")!!)

            db.statementDAO().getSideStatements(position).collect {
                arrayAdapter = ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, it)
                runOnUiThread {
                    initial_statement_List.adapter = arrayAdapter
                }
            }
        }

        initial_statement_List.setOnItemClickListener { parent, view, position, id ->
            val element = parent.getItemAtPosition(position) // The item that was clicked
            //TODO: DElETE STATEMENT...? Will need all in group to vote on it
        }

        //Start Game -> goto hub TODO: This will be a READY! state for this player. only proceed when all players in side are ready.
        initial_statement_start_btn.setOnClickListener{
            updateJob.cancel()
            startActivity(Intent(this, HubActivity::class.java))
        }

        //add a new statement to the quiver
        initial_statement_add_btn.setOnClickListener{
            //create popup with edittext. Debator enters new statement in here.
            val inputDialog  = AlertDialog.Builder(this@InitialStatementsActivity)
            val input = EditText(this@InitialStatementsActivity)
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
                            val newStatement = DatabaseClasses.Statement(0,textContent,position = position, status = 0)
                            db.statementDAO().insertStatement(newStatement)
                            Log.i("InitStatemnets","STATEMENT INSERTED")
                        }
                    }
                })
            inputDialog.show()
        }

    }
}