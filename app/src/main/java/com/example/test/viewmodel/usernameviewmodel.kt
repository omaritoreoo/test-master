package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.test.data.User
import com.example.test.data.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepository(application)

    val loggedInUser: LiveData<User?> = repository.getLoggedInUser().asLiveData()

    fun saveUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveUser(user)
        }
    }

    suspend fun loginUser(email: String, passwordAttempt: String): User? {
        return repository.loginUser(email, passwordAttempt)
    }

    fun getUserByEmail(email: String): LiveData<User?> {
        return repository.getUserByEmail(email).asLiveData()
    }

    fun clearUser() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearUserData()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}