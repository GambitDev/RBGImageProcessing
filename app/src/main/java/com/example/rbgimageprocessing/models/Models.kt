package com.example.rbgimageprocessing.models

data class Color(val r: Int, val g: Int, val b: Int)

data class AnalyzedRBGData(val color: Color, val percentageFromImage: Double)