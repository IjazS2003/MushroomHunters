package com.example.mushroomhunters.ui.recenttrips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecentTripsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Recent Trips Fragment"
    }
    val text: LiveData<String> = _text
}