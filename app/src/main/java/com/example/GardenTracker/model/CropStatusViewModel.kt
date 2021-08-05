package com.example.GardenTracker.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object CropStatusViewModel : ViewModel() {

    val growthProgress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    val waterStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val waterHours: MutableLiveData<ArrayList<Int>> by lazy {
        MutableLiveData<ArrayList<Int>>()
    }

}