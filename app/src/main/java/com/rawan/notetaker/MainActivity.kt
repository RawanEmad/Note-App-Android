package com.rawan.notetaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.rawan.notetaker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.qualifiedName
    private var referred = false
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_ID, auth.currentUser!!.uid)
            startActivity(intent)
        }

        binding.btnSignIn.setOnClickListener {

            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("EG").build()
            )

            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
                    .enableAnonymousUsersAutoUpgrade()
                    .build(), RC_SIGN_IN)
        }

        if (intent.hasExtra(SIGNIN_MESSAGE)) {
            binding.btnSkip.visibility = View.INVISIBLE
            binding.tvMessage.text = intent.getStringExtra(SIGNIN_MESSAGE)
            referred = true
        } else {
            binding.btnSkip.setOnClickListener {
                auth.signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadListActivity()
                        } else {
                            Log.e(TAG, "Anonymously Sign-in failed ", task.exception)
                            Toast.makeText(this, "Sign-in failed ", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun loadListActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val intent = Intent(this, ListActivity::class.java)
        intent.putExtra(USER_ID, user!!.uid)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN){
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK){
                if (referred) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    loadListActivity()
                }
            } else {
                if(response != null && response.error != null && response.error!!.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                    val fullCredential = response.credentialForLinking
                    if (fullCredential != null) {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithCredential(fullCredential)
                            .addOnSuccessListener {
                                if (referred) {
                                    val user = auth.currentUser
                                    val intentData = Intent()
                                    intentData.putExtra(USER_ID, user!!.uid)
                                    setResult(Activity.RESULT_OK, intentData)
                                    finish()
                                } else {
                                    loadListActivity()
                                }
                            }
                    }
                }
            }

            if (resultCode != Activity.RESULT_CANCELED && resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "Sign-in failed ", response?.error)
                Toast.makeText(this, "Sign-in failed ", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val USER_ID = "user_id"
        const val RC_SIGN_IN = 15
        const val SIGNIN_MESSAGE = "signin_message"
    }
}
