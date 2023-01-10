package com.rawan.notetaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.rawan.notetaker.MainActivity.Companion.USER_ID
import com.rawan.notetaker.databinding.ActivityNewNoteBinding

class NewNoteActivity : BaseActivity() {

    private lateinit var binding: ActivityNewNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNewNoteBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        signInMessage = "Sign-in to create new note!"

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

    companion object {
        const val NEW_TITLE = "new_title"
        const val NEW_BODY = "new_body"
    }
}
