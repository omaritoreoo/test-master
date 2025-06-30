package com.example.test.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.example.test.data.AppDatabase
import com.example.test.data.Profile
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val profileDao = AppDatabase.getDatabase(application).profileDao()

    // This MediatorLiveData will hold the Profile data
    private val _profile = MediatorLiveData<Profile?>()
    val profile: LiveData<Profile?> = _profile

    // We'll keep track of the previously added username source to remove it.
    private var currentUsernameSource: LiveData<String?>? = null
    // Call this function from DashboardMainScreen to pass the logged-in username
    fun setLoggedInUsername(usernameLiveData: LiveData<String?>) {
        currentUsernameSource?.let { oldSource ->
            _profile.removeSource(oldSource)
        }

        _profile.addSource(usernameLiveData) { username ->
            if (username != null) {
                val newProfileSource = profileDao.getProfileByUsername(username)

                // Remove the previously observed profile source, if any
                _profile.value?.let { currentProfile ->
                    val oldProfileSource = profileDao.getProfileByUsername(currentProfile.username)
                    _profile.removeSource(oldProfileSource)
                }

                // Add the new profile source
                _profile.addSource(newProfileSource) { profileData ->
                    _profile.value = profileData // Update _profile with the new data
                }
            } else {
                _profile.value?.let { currentProfile ->
                    val oldProfileSource = profileDao.getProfileByUsername(currentProfile.username)
                    _profile.removeSource(oldProfileSource)
                }
                _profile.value = null // Clear the profile
            }
        }
        currentUsernameSource = usernameLiveData
    }

    fun saveProfile(profile: Profile) = viewModelScope.launch {
        profileDao.insertProfile(profile)
    }

    fun updatePhotoUri(uri: String) {
        val currentProfile = _profile.value
        if (currentProfile != null) {
            val updatedProfile = currentProfile.copy(photoUri = uri)
            _profile.value = updatedProfile // Update UI directly for immediate feedback
            saveProfile(updatedProfile)    // Save to database
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}