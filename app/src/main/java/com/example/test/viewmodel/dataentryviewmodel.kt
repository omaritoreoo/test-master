package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.test.data.AppDatabase
import com.example.test.data.DataEntry
import com.example.test.data.DataEntryDatabase
import com.example.test.data.DataEntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "entry-db"
    ).build()

    private val repo = DataEntryRepository(db.dataEntryDao())

    val allEntries = repo.allEntries

    fun insertEntry(entry: DataEntry) {
        viewModelScope.launch {
            repo.insert(entry)
        }
    }
}
