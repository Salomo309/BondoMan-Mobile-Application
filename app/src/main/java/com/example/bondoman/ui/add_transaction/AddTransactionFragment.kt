package com.example.bondoman.ui.add_transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.bondoman.R
import com.example.bondoman.databinding.FragmentAddTransactionBinding
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.transaction.TransactionViewModel
import java.util.Date
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.bondoman.MainActivity
import com.example.bondoman.service.LocationFinder
import com.example.bondoman.service.NetworkStateService
import com.example.bondoman.ui.setting.SettingFragment
import java.io.IOException


class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var randomizationReceiver : BroadcastReceiver
    private var isRandomizationEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fused Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Check Randomizer Condition
        isRandomizationEnabled = (requireActivity() as MainActivity).getIsRandomizationEnabled()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRandomizationReceiver()

        // Transaction View Model
        val transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Check Location Permission
        if (!LocationFinder(requireContext(), requireActivity()).checkLocationPermission()) {
            binding.editTextLokasi.visibility = View.VISIBLE
            binding.editTextLokasiLabel.visibility = View.VISIBLE

            // Request Permission
            requestLocationPermission()
        } else {
            binding.editTextLokasi.visibility = View.GONE
            binding.editTextLokasiLabel.visibility = View.GONE
        }

        // Adapter for spinner (Pemasukan and Pengeluaran)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editCategory.adapter = adapter

        binding.buttonSave.setOnClickListener {
            addTransaction(transactionViewModel)
        }
    }

    override fun onResume() {
        super.onResume()

        if (isRandomizationEnabled) {
            val randomizedTitle = generateRandomTitle()
            binding.editTextJudul.setText(randomizedTitle)
        } else {
            binding.editTextJudul.setText("")
        }
    }

    private fun setupRandomizationReceiver() {
        randomizationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.bondoman.RANDOM_TRANSACTION_ACTION") {
                    isRandomizationEnabled = intent.getBooleanExtra("isRandomizationEnabled", false)
                }
            }
        }
        val filter = IntentFilter("com.example.bondoman.RANDOM_TRANSACTION_ACTION")
        ContextCompat.registerReceiver(requireContext(), randomizationReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun addTransaction(transactionViewModel: TransactionViewModel) {
        val locationFinder = LocationFinder(requireContext(), requireActivity())
        val title = binding.editTextJudul.text.toString()
        val category = binding.editCategory.selectedItem.toString()
        val amount = binding.editTextNominal.text.toString().toDoubleOrNull()

        // Check Fields
        if (title != null) {
            if (title.isNotEmpty() && category.isNotEmpty() && amount != null) {

                // Check location permission
                if (locationFinder.checkLocationPermission()) {

                    // Get Location
                    locationFinder.getDeviceLocation(fusedLocationClient) { latitude, longitude, address ->

                        // Set Transaction ID
                        var id = 1L
                        if (transactionViewModel.listTransactions.value != null) {
                            id = transactionViewModel.listTransactions.value!![transactionViewModel.listTransactions.value!!.size - 1].id + 1L
                        }

                        // Form new TransactionEntity Object
                        val transaction = TransactionEntity(
                            id,
                            "X",
                            title,
                            category,
                            amount,
                            address,
                            longitude,
                            latitude,
                            Date()
                        )

                        Log.d("transaction: ", transaction.toString())

                        // Insert New Transaction
                        transactionViewModel.insertTransaction(transaction)

                        // Reset input fields
                        binding.editTextJudul.text.clear()
                        binding.editTextNominal.text.clear()

                        // Show Success Message
                        Toast.makeText(requireContext(), "Transaction added successfully", Toast.LENGTH_SHORT).show()

                        // Go back to Transaction Fragment
                        findNavController().navigate(R.id.navigation_transaction)
                    }

                } else {
                    // User Does Not Allow Location
                    val address = binding.editTextLokasi.text.toString()

                    if (address.isNotEmpty()) {
                        // Get Location From Location Input
                        getLongLat(requireContext(), address)?.let { (latitude, longitude) ->

                            // Set Transaction ID
                            var id = 1L
                            if (transactionViewModel.listTransactions.value != null) {
                                id = transactionViewModel.listTransactions.value!![transactionViewModel.listTransactions.value!!.size - 1].id + 1L
                            }

                            // Form new TransactionEntity Object
                            val transaction = TransactionEntity(
                                id,
                                "X",
                                title,
                                category,
                                amount,
                                address,
                                longitude,
                                latitude,
                                Date()
                            )

                            // Insert New Transaction
                            transactionViewModel.insertTransaction(transaction)

                            // Reset input fields
                            binding.editTextJudul.text.clear()
                            binding.editTextNominal.text.clear()

                            // Show Success Message
                            Toast.makeText(requireContext(), "Transaction added successfully", Toast.LENGTH_SHORT).show()

                            // Go back to Transaction Fragment
                            findNavController().navigate(R.id.navigation_transaction)
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLongLat(context: Context, location: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context)
        try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                val latitude = addresses[0].latitude
                val longitude = addresses[0].longitude
                Log.d("Location", "Latitude: $latitude, Longitude: $longitude")
                return Pair(latitude, longitude)
            } else {
                Log.e("Location", "No address found for the location: $location")
                return Pair(-6.891272416105667, 107.61072512264901)
            }
        } catch (e: IOException) {
            Log.e("Location", "Error getting location from Geocoder: ${e.message}")
            return null
        }
    }

    private fun generateRandomTitle(): String {
        val titles = arrayOf("Belanja", "Makan Siang", "Transportasi", "Hiburan", "Tagihan", "Laundry", "Uang Kos")
        return titles.random()
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        isGranted: Boolean ->
        if (isGranted) {
            binding.editTextLokasi.visibility = View.GONE
            binding.editTextLokasiLabel.visibility = View.GONE
        } else {
            binding.editTextLokasi.visibility = View.VISIBLE
            binding.editTextLokasiLabel.visibility = View.VISIBLE
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}