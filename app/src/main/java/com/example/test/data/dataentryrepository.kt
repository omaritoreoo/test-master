package com.example.test.data

import kotlinx.coroutines.flow.Flow

class DataEntryRepository(private val dao: DataEntryDao) {
    val allEntries: Flow<List<DataEntry>> = dao.getAllEntries()

    suspend fun insert(entry: DataEntry) = dao.insert(entry)
    suspend fun update(entry: DataEntry) = dao.update(entry)
    suspend fun delete(entry: DataEntry) = dao.delete(entry)
}
