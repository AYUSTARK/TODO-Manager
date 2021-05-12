package com.ayustark.todo_manager.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class],version = 1)
// add the list of entities and the version number
abstract class NoteDatabase : RoomDatabase() {
    abstract val noteDAO : NoteDAO

    /*
        we have only one entity class and corresponding DAO interface
        if we had more entity class we would have listed them all here
        and also in the list on top in the @Database list and define
        references for corresponding DAOs
    */

    /*
        Now , we can just create a new object and use this database class in
        other places of the project. BUT THIS IS NOT A GOOD PRACTICE. Usually
        we should only create only ONE instance of the database object for the
        entire app to avoid unexpected error and performance issues, we shouldn't
        let multiple instance of a database opening at the same time. Therefore we
        create a SINGLETON here.
     */

    //Can ignore this boiler plate code
    companion object{ //in kotlin we use 'companion object' as singleton
        @Volatile //@Volatile makes the field immediately visible to other threads
        private var INSTANCE: NoteDatabase?=null
        fun getInstance(context:Context):NoteDatabase{
            synchronized(this){
                var instance= INSTANCE
                if(instance==null){
                    instance= Room.databaseBuilder(
                        context.applicationContext,
                        NoteDatabase::class.java,
                        "Note_Data_Database"
                    ).build()
                }
                return instance
            }
        }
    }
}
