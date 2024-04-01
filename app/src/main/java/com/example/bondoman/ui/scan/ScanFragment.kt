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
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.Image
import android.net.ConnectivityManager
import android.net.Uri
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
import com.example.bondoman.service.NetworkStateService
import com.example.bondoman.databinding.FragmentScanBinding
import com.example.bondoman.repository.Repository
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.service.LocationFinder
import com.example.bondoman.storage.TokenManager
import com.example.bondoman.ui.transaction.TransactionViewModel
import com.google.android.gms.location.LocationServices
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Date

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageCapture : ImageCapture
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var recyclerView: RecyclerView
    private lateinit var scanItemAdapter: ScanItemAdapter
    private lateinit var scanViewModel: ScanViewModel
    private lateinit var networkStateReceiver : BroadcastReceiver
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)

        recyclerView = binding.scanResult

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkCurrentNetworkState()
        setupNetworkStateReceiver()

        transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Check if permission for camera is granted. If yes, immediately use the camera. If not, request for permission.
        askForCamera()

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
        binding.buttonRetry.setOnClickListener {
            binding.scanCard.visibility = View.GONE
            binding.shadeOverlay.visibility = View.GONE
        }

        binding.buttonSave.setOnClickListener {
            saveScanResult()
            showToast("Scan data saved")
            binding.scanCard.visibility = View.GONE
            binding.shadeOverlay.visibility = View.GONE
        }
    }

    private fun checkCurrentNetworkState() {
        val isConnected = (requireActivity() as MainActivity).getIsConnected()
        if (isConnected) {
            binding.scanNoInternetOverlay.visibility = View.GONE
        } else {
            binding.scanNoInternetOverlay.visibility = View.VISIBLE
        }
    }

    private fun setupNetworkStateReceiver() {
        networkStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NetworkStateService.ACTION_NETWORK_STATE_CHANGE) {
                    val isConnected = intent.getBooleanExtra(NetworkStateService.EXTRA_NETWORK_STATE, false)
                    if (isConnected) {
                        binding.scanNoInternetOverlay.visibility = View.GONE
                    } else {
                        binding.scanNoInternetOverlay.visibility = View.VISIBLE
                    }
                }
            }
        }
        val filter = IntentFilter(NetworkStateService.ACTION_NETWORK_STATE_CHANGE)
        ContextCompat.registerReceiver(requireContext(), networkStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private val cameraRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        {isGranted : Boolean ->
            if (isGranted) {
                startCamera()
                binding.buttonCapture.setOnClickListener {
                    captureImage()
                }
                askForGallery()
            } else {
                showToast("We need your permission to be able to scan image")
                askForGallery()
            }
        }

    private val galleryRequestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        {isGranted : Boolean ->
            if (isGranted) {
                binding.buttonChoose.setOnClickListener {
                    openGallery()
                }
            } else {
                showToast("We need your permission to be able to upload your image")
            }
        }

    private fun askForCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraRequestPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCamera()
            binding.buttonCapture.setOnClickListener {
                captureImage()
            }
            askForGallery()
        }
    }

    private fun askForGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            galleryRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            binding.buttonChoose.setOnClickListener {
                openGallery()
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
                    image.close()
                    coroutineScope.launch {
                        uploadImage(byteArray)
                    }
                }
            })
    }

    private fun openGallery() {
        chooseImage.launch("image/*")
    }

    private val chooseImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri : Uri? ->
        if (uri != null) {
            val byteArray = getUriByteArray(requireContext().contentResolver, uri)
            coroutineScope.launch {
                if (byteArray != null) {
                    uploadImage(byteArray)
                }
            }
        }
    }

    suspend fun uploadImage(byteArray: ByteArray) {
        coroutineScope.launch {
            val activity = requireActivity() as MainActivity
            activity.disableNavBar()
            showLoading()
            try {
                var itemsList = withContext(Dispatchers.IO) {
                    TokenManager.getToken(requireContext())
                        ?.let { Repository().upload(byteArray, it) }
                }
                hideLoading()
                if (itemsList != null) {
                    itemsList.items.forEach{
                        item ->
                        item.price *= 600
                        item.name = item.name.substring(0, 1).uppercase() + item.name.substring(1)
                    }
                    scanViewModel.setItemList(itemsList.items)
                    showScanResultCard()
                }
            } catch (e: Exception) {
                showToast("Scan failed: ${e.message}")
                binding.shadeOverlay.visibility = View.GONE
            } finally {
                hideLoading()
                activity.enableNavBar()
            }
        }
    }

    private fun getUriByteArray(contentResolver: ContentResolver, uri: Uri): ByteArray? {
        var inputStream: InputStream? = null
        val maxSizeBytes = 1 * 1024 * 1024
        try {
            inputStream = contentResolver.openInputStream(uri)
            val buffer = ByteArrayOutputStream()
            val data = ByteArray(1024)
            var bytesRead: Int
            var totalBytesRead = 0
            if (inputStream != null) {
                while (inputStream.read(data).also { bytesRead = it } != -1) {
                    totalBytesRead += bytesRead
                    if (totalBytesRead > maxSizeBytes) {
                        buffer.write(data, 0, bytesRead - (totalBytesRead - maxSizeBytes))
                        break
                    }
                    buffer.write(data, 0, bytesRead)
                }
            }
            return buffer.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            inputStream?.close()
        }
    }

    private fun getImageByteArray(image: Image): ByteArray {
        val planes = image.planes
        val maxSizeBytes = 1 * 1024 * 1024
        val buffer = ByteArray(maxSizeBytes)
        var totalBytesCopied = 0

        for (plane in planes) {
            val planeBuffer = plane.buffer
            val bytesToCopy = minOf(planeBuffer.remaining(), maxSizeBytes - totalBytesCopied)
            planeBuffer.get(buffer, totalBytesCopied, bytesToCopy)
            totalBytesCopied += bytesToCopy

            if (totalBytesCopied >= maxSizeBytes) {
                break
            }
        }

        return buffer
    }

    private fun saveScanResult() {
        val locationFinder = LocationFinder(requireContext(), requireActivity())
        var id = 1L
        if (transactionViewModel.listTransactions.value != null) {
            id = transactionViewModel.listTransactions.value!![transactionViewModel.listTransactions.value!!.size - 1].id + 1L
        }

        if (!locationFinder.checkLocationPermission()) {
            locationFinder.requestLocationPermission()
        }

        if (locationFinder.checkLocationPermission()) {
            locationFinder.getDeviceLocation(LocationServices.getFusedLocationProviderClient(requireActivity())) {
                latitude, longitude, address ->

                val transaction = TransactionEntity(
                    id,
                    "X",
                    "Scan Result",
                    "Pengeluaran",
                    calculateScanResultAmount(),
                    address,
                    longitude,
                    latitude,
                    Date()
                )

                transactionViewModel.insertTransaction(transaction)
            }
        } else {
            val transaction = TransactionEntity(
                id,
                "X",
                "Scan Result",
                "Pengeluaran",
                calculateScanResultAmount(),
                "Jalan Ganesha 10, Lebak Siliwangi, Kecamatan Coblong, Kota Bandung",
                107.610431,
                -6.893109,
                Date()
            )

            transactionViewModel.insertTransaction(transaction)
        }
    }

    private fun calculateScanResultAmount() : Double {
        val itemList = scanViewModel.getItemList().value!!
        var amount = 0.0

        for (item in itemList) {
            amount += item.price * item.qty
        }

        return amount
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading() {
        binding.shadeOverlay.visibility = View.VISIBLE
        binding.loadingBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingBar.visibility = View.GONE
    }

    private fun showScanResultCard() {
        binding.scanCard.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        requireContext().unregisterReceiver(networkStateReceiver)
        _binding = null
    }
}