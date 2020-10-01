package com.example.project1

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hostBtn.setOnClickListener{
            startActivity(Intent(this, Login::class.java))
        }

        joinBtn.setOnClickListener{
            startActivity(Intent(this, WaitActivity::class.java))
        }

        exitBtn.setOnClickListener{
            AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialog, id -> super@MainActivity.onBackPressed() })
                .setNegativeButton("No", null)
                .show()
        }
    }
}