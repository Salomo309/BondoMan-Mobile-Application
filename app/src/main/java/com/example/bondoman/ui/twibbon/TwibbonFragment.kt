package com.example.bondoman.ui.twibbon

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.MainActivity
import com.example.bondoman.R
import com.example.bondoman.databinding.FragmentTwibbonBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlin.math.min

class TwibbonFragment : Fragment() {

    private var _binding : FragmentTwibbonBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture: ImageCapture
    private lateinit var recyclerView: RecyclerView
    private var selectedTwibbon : Int = R.drawable.elysia
    private lateinit var selectedImageOverlay : ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentTwibbonBinding.inflate(inflater, container, false)
        recyclerView = binding.twibbonImages
        selectedImageOverlay = binding.twibbonOverlayImage
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hideTwibbonResult()

        askForCamera()

        val twibbonImageIDs = listOf(R.drawable.elysia, R.drawable.anime1, R.drawable.luke, R.drawable.normal1, R.drawable.normal2, R.drawable.normal3)
        val twibbonImageAdapter = TwibbonImageAdapter(twibbonImageIDs) {
            imageId ->
            selectedTwibbon = imageId
            selectedImageOverlay.setImageResource(selectedTwibbon)
        }
        recyclerView.adapter = twibbonImageAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        selectedImageOverlay.setImageResource(R.drawable.elysia)

        binding.buttonOk.setOnClickListener {
            hideTwibbonResult()
        }
    }

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted : Boolean ->
            if (isGranted) {
                startCamera()
                binding.twibbonButtonCapture.setOnClickListener {
                    createTwibbonImage()
                }
            } else {
                showToast("We need your permission to be able to capture image")
            }
        }

    private fun askForCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
            binding.twibbonButtonCapture.setOnClickListener {
                createTwibbonImage()
            }
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        val preview : Preview = Preview.Builder()
            .build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()

        imageCapture = view?.display?.let {
            ImageCapture.Builder()
                .setTargetRotation(it.rotation)
                .build()
        }!!

        preview.setSurfaceProvider(binding.twibbonPreviewView.surfaceProvider)
        val camera = cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
    }

    private fun createTwibbonImage() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
            object:OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    showToast("Failed to capture image")
                }

                override fun onCaptureSuccess(image: ImageProxy) {
                    showToast("Creating your twibbon...")
                    val capturedImageBitmap = imageProxyToBitmap(image)
                    val combinedBitmap = combineImageWithTwibbon(capturedImageBitmap)
                    binding.twibbonResult.setImageBitmap(combinedBitmap)
                    showTwibbonResult()
                    image.close()
                }
            })
    }

    private fun combineImageWithTwibbon(capturedImageBitmap: Bitmap) : Bitmap {
        val selectedTwibbonBitmap = BitmapFactory.decodeResource(requireContext().resources, selectedTwibbon)
        val scaledTwibbonBitmap = Bitmap.createScaledBitmap(selectedTwibbonBitmap, capturedImageBitmap.width, capturedImageBitmap.height, true)

        val combinedBitmap = Bitmap.createBitmap(capturedImageBitmap.width, capturedImageBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)
        canvas.drawBitmap(capturedImageBitmap, 0f, 0f, null)
        canvas.drawBitmap(scaledTwibbonBitmap, 0f, 0f, null)

        return combinedBitmap
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy) : Bitmap {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        val matrix = Matrix()
        matrix.postScale(-1f, 1f)
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        val size = min(rotatedBitmap.height, rotatedBitmap.width)
        val x = (rotatedBitmap.width - size) / 2
        val y = (rotatedBitmap.height - size) / 2
        val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, x, y, size, size)

        if (croppedBitmap != rotatedBitmap) {
            rotatedBitmap.recycle()
        }

        return croppedBitmap
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showTwibbonResult() {
        binding.twibbonShadeOverlay.visibility = View.VISIBLE
        binding.twibbonCard.visibility = View.VISIBLE
    }

    private fun hideTwibbonResult() {
        binding.twibbonShadeOverlay.visibility = View.GONE
        binding.twibbonCard.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}