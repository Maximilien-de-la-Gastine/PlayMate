package com.isep.PlayMate.ui.add_event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AddEventViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is add event Fragment"
    }
    val text: LiveData<String> = _text
}