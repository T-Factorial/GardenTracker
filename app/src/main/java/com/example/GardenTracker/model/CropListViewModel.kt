package com.example.GardenTracker.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

object CropListViewModel : ViewModel() {
    val cropList: MutableLiveData<ArrayList<Crop>> by lazy {
        MutableLiveData<ArrayList<Crop>>()
    }
}