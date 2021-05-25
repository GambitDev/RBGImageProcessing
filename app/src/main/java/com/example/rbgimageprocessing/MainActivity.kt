package com.example.rbgimageprocessing

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.rbgimageprocessing.databinding.ActivityMainBinding
import com.example.rbgimageprocessing.models.AnalyzedRBGData
import com.example.rbgimageprocessing.models.Color
import com.example.rbgimageprocessing.models.RGBDataViewModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

//    Some of the code in this activity is copied from Android's
//    documentation on their native library CameraX
//    I used CameraX to get access to the camera pipeline
//    https://developer.android.com/codelabs/camerax-getting-started
//    This is the first time I am working with this library so I copied
//    some of the necessary syntax

    private lateinit var binding: ActivityMainBinding
    private val viewModel: RGBDataViewModel by viewModels()
    private val adapter by lazy { ColorAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getPermissionsAndStartCameraFeed()
        setActivityComponents()
        setLiveDataObserver()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted()) {
                startCameraFeed()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun getPermissionsAndStartCameraFeed() {
        if (permissionsGranted()) {
            startCameraFeed()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setActivityComponents() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.recycler.adapter = adapter
    }

    private fun setLiveDataObserver() {
        viewModel.data.observe(this, Observer {
            adapter.data = it
        })
    }

    private fun permissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startCameraFeed() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, RGBAnalyzer(this::onAnalyzingCompleted))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun onAnalyzingCompleted(map: Map<Color, Int>) {
        val analyzedData = handleAnalyzedData(map)
        Handler(Looper.getMainLooper()).post{
            updateViewModelWithNewAnalyzedData(analyzedData)
        }
    }

    private fun updateViewModelWithNewAnalyzedData(data: List<AnalyzedRBGData>) {
        viewModel.data.value = data
    }

    private fun handleAnalyzedData(map: Map<Color, Int>): List<AnalyzedRBGData> {
        val totalNumberOfPixels = binding.preview.width * binding.preview.height
        val analyzedData = mutableListOf<AnalyzedRBGData>()
        for (key in map.keys) {
            val percentage =
                100 * (map[key] ?: error("")).toDouble() / totalNumberOfPixels.toDouble()
            val formattedPercentage = String.format("%.2f", percentage).toDouble()
            analyzedData.add(AnalyzedRBGData(key, formattedPercentage))
        }
        return analyzedData.sortedByDescending { it.percentageFromImage }
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val TAG = "RGBImageProcessing"
        private lateinit var cameraExecutor: ExecutorService
    }
}