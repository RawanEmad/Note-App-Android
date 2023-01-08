package com.rawan.notetaker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rawan.notetaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSignIn.setOnClickListener {}

        val intent = Intent(this, ListActivity::class.java)
        intent.putExtra(USER_ID, "-1")
        startActivity(intent)
    }

    companion object {
        const val USER_ID = "user_id"
    }
}
