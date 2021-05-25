package com.example.rbgimageprocessing.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RGBDataViewModel : ViewModel() {
    val data: MutableLiveData<List<AnalyzedRBGData>>
            by lazy {MutableLiveData<List<AnalyzedRBGData>>()}
}