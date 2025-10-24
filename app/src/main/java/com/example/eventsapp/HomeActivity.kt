package com.example.eventsapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val goToEventButton: Button = findViewById(R.id.goToEventButton)

        goToEventButton.setOnClickListener {
            val intent = Intent(this, EventActivity::class.java)
            startActivity(intent)
        }
    }
}