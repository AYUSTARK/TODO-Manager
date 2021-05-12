package com.ayustark.todo_manager.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
//Room will recognize this interface as a DataAccessObject
interface NoteDAO {

    /*
    - Function name is not important the annotation @insert is important
    - As we have inserted a Note Object Room will recognize that it will have
      to insert note object to the database and will generate the required code
      in the back ground
    - We have 'suspend' modifier since Room doesn't support accessing data in
      the main thread since it may block it for large amount of time so we
      have to execute it in background thread. Instead we are using kotlin
      coroutine, these are special function that can be suspended and
      resumed at later time
    - You may not use suspend modifier if you are using some other form of
      background processing
    - Sometimes for verification you may need to return value, we can actually get the
      new row id/ids as a return value of type Long/Array of lOng/List of long values
      Example:
      -   @Insert
          suspend fun insertNote(note:Note):Long
      -   @Insert
          suspend insertNotes(note1:Note,note2:Note,note3:Note) : List<Long>
    */

    @Insert(onConflict = OnConflictStrategy.REPLACE) //on conflict is sometimes important
    fun insertNote(note:Note):Long  //return the id

    @Update
    fun updateNote(note:Note):Int// may return int, ie the number of rows updated

    @Delete
    fun deleteNote(note:Note):Int// may return int, ie the number of rows deleted

    @Query("DELETE FROM Notes_Data_Table")//Table name given at Note Entity Class
    fun deleteAll():Int //These SQL re queries are evaluated at compile time so no issues related to run time SQL problem

    @Query("SELECT * FROM Notes_Data_Table")
    fun getAllNotes():LiveData<List<Note>>
    /*
     Room facilitates us to get data as LiveData of list of entities(LiveDat<List<Notes>>)
     these queries are called async queries,which have LiveData as return value room always
     runs them in the background thread and update the list. No suspend modifier required
     room runs it itself.
     */
}