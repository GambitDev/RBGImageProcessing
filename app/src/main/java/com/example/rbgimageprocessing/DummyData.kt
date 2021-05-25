package com.example.rbgimageprocessing

import com.example.rbgimageprocessing.models.AnalyzedRBGData
import com.example.rbgimageprocessing.models.Color

class DummyData {
    companion object {
        private val dummyColor = Color(255, 255, 255)
        private const val dummyPercentage = 0.0
        private val dummyList = arrayListOf<AnalyzedRBGData>()

        fun getDummyList(): List<AnalyzedRBGData> {
            for(i in 0..4) {
                dummyList.add(AnalyzedRBGData(dummyColor, dummyPercentage))
            }
            return dummyList
        }
    }
}