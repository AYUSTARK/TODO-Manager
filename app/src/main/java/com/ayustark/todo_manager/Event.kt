package com.ayustark.todo_manager


/*
    In different situations we might need to communicate with View from ViewModel
    to inform the user about a result. Example: In this project we might need to
    inform the user after successful insert note data in to database.
    One way,
    -> Making a separate functions and make the Activity implement and invoking
       those functions from the ViewModel. But that is not a correct approach since
       it violate teh fundamentals of MVVM ideally the ViewModel shouldn't know about
       the View, shouldn't have any reference to the Views
    Another way
    -> We can use LiveData for events by creating a single live event class, the
     problem with  single live event is that it's restricted to one observer. This
     LiveData will only send an update once

      ListenEventTracker is the recommended best practice. It allows us to explicitly
      consider the state of the event

 */

//This class will be used as a Wrapper for data that is exposed via live data that represents an event
// This is a boiler plate code
open class Event<out T> (private val content:T){
    var hasBeenHandled=false
    private set //Allow external read but not write
    /**
     * Returns the content and prevents its use again
     */
    fun getContentIfNOtHandled(): T?{
        return if(hasBeenHandled){
            null
        }else{
            hasBeenHandled=true
            content
        }
    }
    /**
     * Returns teh contents even if it is already been handled
     */
    fun peekContent():T=content
}