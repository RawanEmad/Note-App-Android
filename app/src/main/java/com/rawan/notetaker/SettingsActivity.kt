package com.rawan.notetaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.UserProfileChangeRequest
import com.rawan.notetaker.databinding.ActivitySettingsBinding

class SettingsActivity : BaseActivity(){

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        signInMessage = "Sign-in to see your settings"
        val authUI = AuthUI.getInstance()

        binding.btnSignOut.setOnClickListener{
            authUI
                .signOut(this)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Log.e(TAG, "Sign-out failed", task.exception)
                        Toast.makeText(this, "Sign-out failed", Toast.LENGTH_LONG).show()
                    }
                }
        }

        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("This is permanent, are you sure?")
                .setPositiveButton("Yes") { _,_ ->
                    authUI.delete(this)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful) {
                                startActivity(Intent(this, MainActivity::class.java))
                            } else {
                                Log.e(TAG, "Delete Account failed", task.exception)
                                Toast.makeText(this, "Delete Account failed", Toast.LENGTH_LONG).show()
                            }
                        }
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.etName.setText(user?.displayName)
    }

    override fun onPause() {
        super.onPause()
        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(binding.etName.text.toString())
            .build()

        if (user != null) {
            user!!.updateProfile(profile)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "Failed to update display name ", task.exception)
                        Toast.makeText(this, "Failed to update name ", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    companion object {
        private val TAG = SettingsActivity::class.qualifiedName
    }
}