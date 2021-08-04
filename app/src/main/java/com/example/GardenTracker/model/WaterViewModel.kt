package com.example.GardenTracker.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WaterViewModel : ViewModel() {

    val waterStatus: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    val waterHours: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
}