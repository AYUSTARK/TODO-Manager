package com.ayustark.todo_manager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ayustark.todo_manager.db.Note
import kotlinx.android.synthetic.main.list_item.view.*

//List of Notes as parameter
// Unit here signifies this function doesn't return anything
class NoteRecyclerViewAdapter(
    private val context: Context,
    private val clickListener: (Note) -> Unit
) : RecyclerView.Adapter<MyViewHolder>() {

    private val noteList = ArrayList<Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(noteList[position], clickListener)
    }

    override fun getItemCount(): Int {
        return noteList.size
    }

    //function to set the list of subscribers to set to notes
    fun setList(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
    }

}

/*
   As you know this class should extend the recyclerviewAdapter
   class when we are extending we need to provide an adapter
   class as object type. Therefore, we need to create another
   class which extends these recycler view adapter class. Hence
   creating a separate class.
   */
class MyViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    /*
    We mainly use this class to bind values to each list item
     */
    fun bind(note: Note, clickListener: (Note) -> Unit) {
        view.note_title_textView.text = note.title
        view.note_description_textView.text = note.description
        view.noteDate.text = note.date
        Log.d("NOTE ID", "${note.id}")
        view.list_item_layout.setOnClickListener {
            // Code to pass the selected Note Instance this will pass the ListItemClicked function in the main activity
            clickListener(note)
        }
    }

}