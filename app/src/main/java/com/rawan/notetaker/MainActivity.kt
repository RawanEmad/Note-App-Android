package com.rawan.notetaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.rawan.notetaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.qualifiedName
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnSignIn.setOnClickListener {

            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                .build(), RC_SIGN_IN)
        }

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_ID, auth.currentUser!!.uid)
            startActivity(intent)
        }
    }

    companion object {
        const val USER_ID = "user_id"
        const val RC_SIGN_IN = 15
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK){
                val user = FirebaseAuth.getInstance().currentUser
                val intent = Intent(this, ListActivity::class.java)
                intent.putExtra(USER_ID, user!!.uid)
                startActivity(intent)
            } else {
                Log.e(TAG, "Sign-in failed ", response!!.error)
            }
        }
    }
}
