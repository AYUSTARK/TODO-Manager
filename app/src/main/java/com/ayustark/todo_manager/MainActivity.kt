package com.ayustark.todo_manager

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.ayustark.todo_manager.db.Note
import com.ayustark.todo_manager.db.NoteDAO
import com.ayustark.todo_manager.db.NoteDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: NoteRecyclerViewAdapter
    private lateinit var dao: NoteDAO
    private var isUpdateOrDelete = false
    private lateinit var noteToUpdateOrDelete: Note
    private lateinit var today: LocalDate
    private lateinit var calData: LocalDate
    private val channelId = "TODO"
    private lateinit var notificationManager: NotificationManager

    //    var noteList = arrayListOf<Note>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /*
           Then we need a reference variable for a NoteViewModel instance, bur before
           getting the viewModel Instance using the Factory class we need to create a
           ViewModelFactory instance , to create a NoteViewModelFactory instance we
           need to pass a DAO instance as an argument. Let's make a DAO instance
           first
        */
        //We already created a room database class which provides a singleton database instance
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Due tasks",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        dao = NoteDatabase.getInstance(application).noteDAO
        val cal = Calendar.getInstance()
        today = LocalDate.parse(
            "${cal.get(Calendar.YEAR)}-${addZero(cal.get(Calendar.MONTH) + 1)}-${
                addZero(cal.get(Calendar.DAY_OF_MONTH))
            }"
        )
        calData = today
        noteDate.text = calData.toString()
        noteDate.setOnClickListener {
            val dpd = DatePickerDialog(this@MainActivity, { _, year, month, day ->
                val date = LocalDate.parse("$year-${addZero(month + 1)}-${addZero(day)}")
//                Log.d("Date", "$date $today")
                noteDate.text = date.toString()
//                Log.d("DATE", " ${date.minus(today)} $today $year ${month + 1} $day")
            }, calData.year, calData.monthValue - 1, calData.dayOfMonth)
            dpd.create()
            dpd.show()
        }
        initRecyclerView()
        if (intent.getBooleanExtra("notified", false)) {
            listItemClicked(
                Note(
                    intent.getIntExtra("id", 0),
                    intent.getStringExtra("title")!!,
                    intent.getStringExtra("desc")!!,
                    intent.getStringExtra("date")!!
                )
            )
        }
        /*noteDate.init(2020, 11, 22) { view, year, month, day ->
            val sdf = SimpleDateFormat("dd-MM-YYYY", Locale.getDefault())
//            val format = sdf.format(Calendar.getInstance().set(year, month, day))
//            val date = sdf.parse(format)
            val date2 = LocalDate.parse("$year-${addZero(month + 1)}-${addZero(day)}")
            val today = LocalDate.parse("2020-11-18")
            Log.d("DATE", " ${date2.minus(today)} $today $year ${month + 1} $day")
        }*/
    }

    private fun initRecyclerView() {
        noteRecyclerView.layoutManager = GridLayoutManager(this@MainActivity, 2)
        //To minimize code inconsistency we will only create only one adapter
        adapter = NoteRecyclerViewAdapter(this@MainActivity) { selectedItem: Note ->
            listItemClicked(selectedItem)
        }
        noteRecyclerView.adapter = adapter
        displayNotesList()
    }


    //Creating a function to observe the list of data in database table
    private fun displayNotesList() {
        val notes = dao.getAllNotes()
        notes.observe(this@MainActivity) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
            checkDue(it)
        }
    }

    private fun checkDue(notes: List<Note>) {
        notificationManager.cancelAll()
        Log.e("Notes", notes.toString())
        var i = 0
        for (note in notes) {
            if (LocalDate.parse(note.date).minus(today)) {
//                Toast.makeText(this@MainActivity, "${note.title} is due", Toast.LENGTH_SHORT).show()
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("notified", true).putExtra("id", note.id)
                    .putExtra("title", note.title).putExtra("desc", note.description)
                    .putExtra("date", note.date)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this, i, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val bundle = Bundle()
                bundle.putBoolean("notified", true)
                bundle.putInt("id", note.id)
                bundle.putString("title", note.title)
                bundle.putString("desc", note.description)
                bundle.putString("date", note.date)
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("TODO")
                    .setContentText("${note.title} is due")
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .setContentIntent(pendingIntent)
                    .addExtras(bundle)
                notificationManager.notify(i, notificationBuilder.build())
                i++
//                break
            }
        }
    }

    private fun listItemClicked(note: Note) {
        //Toast.makeText(this,"Selected name is ${note.title}",Toast.LENGTH_LONG).show()
        Log.d("Notification", note.toString())
        initUpdateAndDelete(note)
    }

    private fun initUpdateAndDelete(note: Note) {
        //display the selected tile and description in the testInputField
        noteTitle.setText(note.title)
        noteDescription.setText(note.description)
        noteDate.text = note.date
        calData = LocalDate.parse(note.date)
//        noteDate.init(date.year,date.monthValue,date.dayOfMonth,null)

        //update the bool and give the note instance
        isUpdateOrDelete = true
        noteToUpdateOrDelete = note

        //Change display value of btns
        saveOrUpdateBtn.text = "Update"
        clearAllBtn.text = "Delete"
        //implement methods
    }

    private fun insert(note: Note) {
        // now call the insert function of repository passing the note instance
        // we should make this function call from a background thread
        //return row id for more confirmations
        if (checkInput()) {
            GlobalScope.launch {
                val newRowId = dao.insertNote(note)
                if (newRowId > -1) { //return -1 on fail
                    //change the mutable live data with success message
                    // Not necessary but for learning purpose important
                    runOnUiThread {
                        displayNotesList()
                        showToast("Note Inserted Successfully IDno: $newRowId")
                    }
                } else {
                    showToast("Error Obscured in Inserting ")
                }
            }
        }
    }

    private fun checkInput(): Boolean {
        return if (noteTitle.text.toString().isNotEmpty() &&
            noteDescription.text.toString().isNotEmpty()
        ) {
            true
        } else {
            showToast("Give Input")
            false
        }
    }

    private fun showToast(s: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT).show()
        }
    }

    fun saveOrUpdate(view: View) {
        //Adding validations on the inputted text
        if (noteTitle.text == null) {
            showToast("Please enter Note Title")
        } else if (noteDescription == null) {
            showToast("Please enter Note Description")
        } else {
            //boolean to check if this in update and delete state
            if (isUpdateOrDelete) {
                //set the new updated value of notes object
                noteToUpdateOrDelete.title = noteTitle.text.toString()
                noteToUpdateOrDelete.description = noteDescription.text.toString()
                update(noteToUpdateOrDelete)
            } else {
                val title = noteTitle.text.toString()
                val description = noteDescription.text.toString()
                val date = noteDate.text.toString()
                insert(
                    Note(
                        0,
                        title,
                        description,
                        date
                    )
                )
                //after setting the value changing it to null
                noteTitle.text = null
                noteDescription.text = null
                calData = today
                noteDate.text = calData.toString()
            }
        }

    }

    fun clearAllOrDelete(view: View) {
        if (isUpdateOrDelete) {
            delete(noteToUpdateOrDelete)
        } else {
            clearAll()
        }
    }

    private fun update(note: Note) {
        if (checkInput()) {
            GlobalScope.launch {
                val numberOfRowsUpdated = dao.updateNote(note)
                if (numberOfRowsUpdated > 0) {
                    //after update changing back ot normal conditions
                    runOnUiThread {
                        displayNotesList()
                        noteTitle.text = null
                        noteDescription.text = null
                        calData = today
                        noteDate.text = calData.toString()
                        isUpdateOrDelete = false
                        noteToUpdateOrDelete = note
                        saveOrUpdateBtn.text = "Save"
                        clearAllBtn.text = "Clear All"
                        //change the mutable live data with success message
                    }
                    showToast(" $numberOfRowsUpdated rows of Updated Successfully")
                } else {
                    showToast("Error Occurred during Update")
                }
            }
        }
    }

    private fun delete(note: Note) {
        GlobalScope.launch {
            val noOfRowsDeleted = dao.deleteNote(note)
            if (noOfRowsDeleted > 0) {
                //after delete changing back ot normal conditions
                runOnUiThread {
                    displayNotesList()
                    noteTitle.text = null
                    noteDescription.text = null
                    calData = today
                    noteDate.text = calData.toString()
                    isUpdateOrDelete = false
                    noteToUpdateOrDelete = note
                    saveOrUpdateBtn.text = "Save"
                    clearAllBtn.text = "Clear All"
                }
                //change the mutable live data with success message
                showToast("$noOfRowsDeleted rows Deleted Successfully")
            } else {
                showToast("Error Occurred during deleting")
            }
        }
    }

    private fun clearAll() {
        GlobalScope.launch {
            val noOfRowsDeleted = dao.deleteAll()
            if (noOfRowsDeleted > 0) {
                //change the mutable live data with success message
                showToast("$noOfRowsDeleted rows Deleted Successfully")
            } else {
                showToast("Error Occurred during deleting")
            }
        }
    }

    private operator fun LocalDate.minus(today: LocalDate): Boolean {
        return if (this.year < today.year) {
            true
        } else if (this.year > today.year) {
            false
        } else if (this.monthValue < today.monthValue) {
            true
        } else if (this.monthValue > today.monthValue) {
            false
        } else this.dayOfMonth - today.dayOfMonth <= 3
    }

    private fun addZero(value: Int): String {
        return if (value <= 9) {
            "0$value"
        } else {
            "$value"
        }
    }
}