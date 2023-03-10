package com.rawan.notetaker.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.rawan.notetaker.R
import com.rawan.notetaker.data.Note
import com.rawan.notetaker.data.NoteViewModel
import com.rawan.notetaker.databinding.ActivityListBinding
import java.util.*

class ListActivity : AppCompatActivity() {
    private val TAG = ListActivity::class.qualifiedName

    private lateinit var binding: ActivityListBinding

    private lateinit var noteViewModel: NoteViewModel
    private lateinit var adapter: ListRecyclerAdapter
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var firestoreNotesListener: ListenerRegistration
    private lateinit var announcementsCollection: CollectionReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        userId = getUserId()

        binding.fab.setOnClickListener {
            val activityIntent = Intent(this, NewNoteActivity::class.java)
            startActivityForResult(activityIntent, NEW_NOTE_ACTIVITY_REQUEST_CODE)
        }

        loadData()
    }

    private fun getUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: "-1"

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == NEW_NOTE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val id = UUID.randomUUID().toString()
            val title = data!!.getStringExtra(NewNoteActivity.NEW_TITLE)
            val body = data.getStringExtra(NewNoteActivity.NEW_BODY)

            val note = Note(id, title!!, body!!, Calendar.getInstance().timeInMillis, false)

            if (userId != getUserId()) {
                userId = getUserId()
                loadData()
            }

            if (userId == "-1") {
                noteViewModel.insert(note)
            } else {
                addNoteToFirestore(note, firestoreDB.collection(userId))
            }

            Toast.makeText(
                applicationContext,
                R.string.saved,
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                applicationContext,
                R.string.not_saved,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //region data code
    private fun parseDocument(document: DocumentSnapshot): Note {
        return Note(
            document.id,
            document.getString("title")!!,
            document.getString("body")!!,
            document.getLong("date")!!,
            document.getBoolean("announcement")!!
        )
    }

    private fun addNoteToFirestore(note: Note, collection: CollectionReference) {
        collection
            .add(note)
            .addOnSuccessListener { result ->
                Log.d(TAG, "Note added with ID:" + result.id)
                if (note.announcement) {
                    adapter.addNote(note)
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "Error adding note", e) }
    }

    private fun loadData() {
        noteViewModel = ViewModelProvider(this)[NoteViewModel::class.java]
        adapter = ListRecyclerAdapter(this)
        binding.contentList.listNotes.layoutManager = LinearLayoutManager(this)
        binding.contentList.listNotes.adapter = adapter

        firestoreDB = FirebaseFirestore.getInstance()
        announcementsCollection = firestoreDB.collection("announcements")

        announcementsCollection
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Retrieving announcements")
                val announcementsList = ArrayList<Note>()
                for (document in result) {
                    announcementsList.add(parseDocument(document))
                }

                if (announcementsList.size == 0) {
                    seedAnnouncements()
                } else {
                    adapter.addNotes(announcementsList)
                    loadNotes()
                }

            }
            .addOnFailureListener { e -> Log.e(TAG, "Error getting announcements", e) }

    }

    private fun seedAnnouncements() {
        var note =
            Note(
                "",
                "Welcome to Note Taker",
                "This is a great way to learn about Firebase Authentication",
                Calendar.getInstance().timeInMillis,
                true
            )

        addNoteToFirestore(note, announcementsCollection)

        note = Note(
            "",
            "The Sparks Foundation",
            "Social Media Integration from Web and Mobile Development tasks",
            Calendar.getInstance().timeInMillis - 10000,
            true
        )

        addNoteToFirestore(note, announcementsCollection)
    }

    private fun loadNotes() {
        if (userId == "-1") {
            noteViewModel.allNotes.observe(this, Observer { notes ->
                notes?.let {
                    adapter.clearNotes(false)
                    adapter.addNotes(notes)
                }
            })
        } else {
            firestoreNotesListener = firestoreDB.collection(userId)
                .addSnapshotListener(EventListener { snapshots, e ->
                    if (e != null) {
                        Log.e(TAG, "Failed to listen for new notes", e)
                        return@EventListener
                    }

                    for (dc in snapshots!!.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            adapter.addNote(parseDocument(dc.document))
                        }
                    }
                })
        }
    }

    //endregion

    companion object {
        private const val NEW_NOTE_ACTIVITY_REQUEST_CODE = 1


    }
}
