package com.example.GardenTracker.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.GardenTracker.database.DatabaseGateway

class CropListViewModel(var db: DatabaseGateway) : ViewModel() {

    private var mObservableCrops : MediatorLiveData<List<Crop>> = MediatorLiveData<List<Crop>>()

    init {

        mObservableCrops.value = null

        var crops : LiveData<List<Crop>>? = db.getAllLiveCrops()

        if (crops != null) {
            mObservableCrops.addSource(crops, Observer<List<Crop>>() {
                mObservableCrops.value = it
            })
        }

    }

    fun getCrops() : LiveData<List<Crop>> { return mObservableCrops }

}