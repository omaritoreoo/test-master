import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.data.AppDatabase
import com.example.test.data.DataEntry
import com.example.test.data.DataEntryRepository
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataEntryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataEntryRepository(AppDatabase.getDatabase(application).dataEntryDao())
    val allEntries: StateFlow<List<DataEntry>> = repository.allEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selectedEntry = mutableStateOf<DataEntry?>(null)

    fun insertEntry(entry: DataEntry) = viewModelScope.launch {
        // Simpan entry dan ambil ID yang dihasilkan
        val generatedId = repository.insert(entry)
        // Update entry dengan ID yang dihasilkan
        selectedEntry.value = entry.copy(id = generatedId.toInt())  // Pastikan untuk mengupdate state
    }

    fun updateEntry(entry: DataEntry) = viewModelScope.launch {
        repository.update(entry)
    }

    fun deleteEntry(entry: DataEntry) = viewModelScope.launch {
        repository.delete(entry)
    }

    fun setSelectedEntry(entry: DataEntry) {
        selectedEntry.value = entry
    }
    suspend fun insertEntryAndGetId(entry: DataEntry): Long {
        return repository.insert(entry) // repository panggil DAO.insert, dapatkan rowId
    }

}
