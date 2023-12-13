package com.isep.PlayMate.ui.join_event

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class JoinEventViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is join event Fragment"
    }
    val text: LiveData<String> = _text
}