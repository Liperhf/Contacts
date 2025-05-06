package com.example.contactsapp

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsViewModel(application : Application) : AndroidViewModel(application) {
    private var contactService : IContactService? = null
    private val _contacts = mutableStateListOf<ContactInfo>()
    val contacts:List<ContactInfo> = _contacts

    private val _status = mutableStateOf<String?>(null)
    val status: State<String?> = _status

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            contactService = IContactService.Stub.asInterface(service)
            loadContacts()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            contactService = null
        }
    }


    fun bindService(){
        val intent = Intent(getApplication(),Class.forName("com.example.contactsapp.ContactService"))
        getApplication<Application>().bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE)
    }

    fun unbindService(){
        try{
            getApplication<Application>().unbindService(serviceConnection)
        }
        catch (e:Exception){
        }
    }


    fun loadContacts(query : String = ""){
        viewModelScope.launch(Dispatchers.IO){
            try{
                val result = contactService?.searchContacts(query) ?: emptyList()
                _contacts.clear()
                _contacts.addAll(result)
                } catch (e:Exception){
                    _status.value = "Contact loading error:${e.message}"
            }
        }
    }

    fun deleteDuplicates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                contactService?.deleteDuplicatesContacts()
                val hadDuplicates = contactService?.hasDuplicates() ?: false
                _status.value = if (hadDuplicates) {
                    "Duplicates were and removed"
                } else {
                    "No duplicates was found"
                }
                loadContacts()
            } catch (e: Exception) {
                _status.value = "Removal error: ${e.message}"
            }
        }
    }

    fun getGroupedContacts() : Map<Char,List<ContactInfo>>{
        return contacts
            .sortedBy { it.name.lowercase() }
            .groupBy { (it.name.firstOrNull()?.uppercaseChar() ?: "#") as Char }
    }

    fun clearStatus(){
        _status.value = null
    }
}

