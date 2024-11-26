package com.example.mushroomhunters.ui.mushrooms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MushroomViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is mushrooms Fragment"
    }
    val text: LiveData<String> = _text
}