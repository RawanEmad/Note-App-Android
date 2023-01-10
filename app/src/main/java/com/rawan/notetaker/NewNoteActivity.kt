package com.rawan.notetaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.rawan.notetaker.MainActivity.Companion.USER_ID
import com.rawan.notetaker.databinding.ActivityNewNoteBinding

class NewNoteActivity : AppCompatActivity() {

    var userId = "-1"
    private lateinit var binding: ActivityNewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val user = FirebaseAuth.getInstance().currentUser

        if (user == null || user.isAnonymous) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.SIGNIN_MESSAGE, "Sign-in to create new note")
            startActivityForResult(intent, ATTEMPT_SIGNIN)
        }

        binding.btnSave.setOnClickListener {
            val resultIntent = Intent()

            if (TextUtils.isEmpty(binding.etTitle.text) || TextUtils.isEmpty(binding.etBody.text)) {
                setResult(Activity.RESULT_CANCELED, resultIntent)
            } else {
                val title = binding.etTitle.text.toString()
                val body = binding.etBody.text.toString()

                resultIntent.putExtra(NEW_TITLE, title)
                resultIntent.putExtra(NEW_BODY, body)
                resultIntent.putExtra(USER_ID, userId)
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ATTEMPT_SIGNIN && resultCode == Activity.RESULT_CANCELED) {
            finish()
        } else {
            if (data != null && data.hasExtra(USER_ID)) {
                userId = data.getStringExtra(USER_ID).toString()
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val NEW_TITLE = "new_title"
        const val NEW_BODY = "new_body"
        const val ATTEMPT_SIGNIN = 10
    }
}
