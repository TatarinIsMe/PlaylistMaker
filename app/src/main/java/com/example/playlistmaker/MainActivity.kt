package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val but = findViewById<Button>(R.id.search)
        val butMedia = findViewById<Button>(R.id.media)
        val butSettings = findViewById<Button>(R.id.settings)
        but.setOnClickListener(this@MainActivity)
        butMedia.setOnClickListener {
//            Toast.makeText(this@MainActivity, "Нажали на кнопку!", Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, MediaActivity::class.java)
            startActivity(displayIntent)
        }
        butSettings.setOnClickListener {
//            Toast.makeText(this@MainActivity, "Нажали на кнопку!", Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.search -> {
                val displayIntent = Intent(this, SearchActivity::class.java)
                startActivity(displayIntent)
            }
        }
    }
}