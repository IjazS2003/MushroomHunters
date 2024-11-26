package com.example.mushroomhunters.ui.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TripPageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Trip Fragment"
    }
    val text: LiveData<String> = _text
}