package com.example.bondoman.ui.scan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest
import android.content.pm.PackageManager
import android.media.Image
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bondoman.MainActivity
import com.example.bondoman.databinding.FragmentScanBinding
import com.example.bondoman.models.Item
import com.example.bondoman.repository.Repository
import com.example.bondoman.storage.TokenManager
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture : ImageCapture
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanItemAdapter: ScanItemAdapter
    private lateinit var scanViewModel: ScanViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        recyclerView = binding.scanResult

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if permission for camera is granted. If yes, immediately use the camera. If not, request for permission.
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
        }

        // Setup View Model and Recycler View
        scanViewModel = ViewModelProvider(this)[ScanViewModel::class.java]
        scanItemAdapter = ScanItemAdapter(emptyList())
        recyclerView.adapter = scanItemAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        scanViewModel.getItemList().observe(viewLifecycleOwner) { itemList ->
            scanItemAdapter.setItemList(itemList)
            scanItemAdapter.notifyDataSetChanged()
        }

        // Set event listener
        binding.buttonCapture.setOnClickListener {
            captureImage()
        }

        binding.buttonRetry.setOnClickListener {
            binding.scanCard.visibility = View.GONE
        }

        binding.buttonSave.setOnClickListener {
            showToast("Data Saved")
            binding.scanCard.visibility = View.GONE
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        {isGranted : Boolean ->
            if (isGranted) {
                startCamera()
            } else {
                showToast("We need your permission to be able to scan image")
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
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = view?.display?.let {
            ImageCapture.Builder()
                .setTargetRotation(it.rotation)
                .build()
        }!!

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview)
    }

    private fun captureImage() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
            object:OnImageCapturedCallback() {
                override fun onError(exception: ImageCaptureException) {
                    showToast("Failed to capture image")
                }

                @OptIn(ExperimentalGetImage::class) override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    val image = imageProxy.image
                    val byteArray = image?.let { getImageByteArray(it) }!!
                    coroutineScope.launch {
                        val activity = requireActivity() as MainActivity
                        activity.disableNavBar()
                        showLoading()
                        try {
                            val itemsList = withContext(Dispatchers.IO) {
                                TokenManager.getToken(requireContext())
                                    ?.let { Repository().upload(byteArray, it) }
                            }
                            hideLoading()
                            if (itemsList != null) {
                                scanViewModel.setItemList(itemsList.items)
                                showScanResultCard()
                            }
                        } catch (e: Exception) {
                            showToast("Scan failed: ${e.message}")
                        } finally {
                            hideLoading()
                            activity.enableNavBar()
                        }
                    }
                }
            })
    }

    private fun getImageByteArray(image: Image): ByteArray {
        val plane = image.planes.first()
        val buffer = ByteArray(plane.buffer.remaining())
        plane.buffer.get(buffer)

        return buffer
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.shadeOverlay.visibility = View.VISIBLE
        binding.loadingBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.shadeOverlay.visibility = View.GONE
        binding.loadingBar.visibility = View.GONE
    }

    private fun showScanResultCard() {
        binding.scanCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        _binding = null
    }
}