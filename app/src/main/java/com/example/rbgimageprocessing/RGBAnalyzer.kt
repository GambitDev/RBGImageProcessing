package com.example.rbgimageprocessing

import android.graphics.*
import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.core.graphics.blue
import androidx.core.graphics.get
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.rbgimageprocessing.models.Color
import java.io.ByteArrayOutputStream


class RGBAnalyzer(
    private val listener: (Map<Color, Int>) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        image.image?.toBitmap()?.let {
            val rgbValues = getRGBValues(it)
            listener(rgbValues)
        }
        image.close()
    }

//    This function was copied from the accepted answer to this question:
//    https://stackoverflow.com/questions/56772967/converting-imageproxy-to-bitmap
//    CameraX pipeline provides images in YUV format, it was necessary to convert it to bitmap,
//    I used this function since I couldn't figure out how to accomplish this goal by myself
    private fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val vuBuffer = planes[2].buffer // VU

        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()

        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun getRGBValues(bitmap: Bitmap): Map<Color, Int> {
        val occurrencesByRGBValue = mutableMapOf<Color, Int>()
        val height = bitmap.height
        val width = bitmap.width

        for (h in 0 until height) {
            for (w in 0 until width) {
                val r = bitmap[w, h].red
                val g = bitmap[w, h].green
                val b = bitmap[w, h].blue

                val color = Color(r, g, b)
                occurrencesByRGBValue[color]?.let {
                    occurrencesByRGBValue[color] = it + 1
                }
                if (occurrencesByRGBValue[color] == null) {
                    occurrencesByRGBValue[color] = 1
                }
            }
        }

        return getMostRepeatedValues(occurrencesByRGBValue)
    }

    private fun getMostRepeatedValues(map: MutableMap<Color, Int>)
            : Map<Color, Int> {
        val output = mutableMapOf<Color, Int>()
        for (i in 0..4) {
            map.maxBy { it.value }?.also {
                output[it.key] = it.value
                map.remove(it.key)
            }
        }
        return output
    }
}