package com.example.bondoman.ui.add_transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.R
import com.example.bondoman.databinding.FragmentAddTransactionBinding
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bondoman.repository.TransactionRepository
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.transaction.TransactionViewModel
import java.util.Date
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Address
import android.location.Geocoder
import androidx.navigation.fragment.findNavController


class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        fun newInstance() = AddTransactionFragment()
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editCategory.adapter = adapter

        checkLocationPermission()

        binding.buttonSave.setOnClickListener {
            addTransaction()
        }

        return root
    }

    private fun addTransaction() {
        val title = binding.editTextJudul.text.toString()
        val category = binding.editCategory.selectedItem.toString()
        val amount = binding.editTextNominal.text.toString().toDoubleOrNull()

        if (title.isNotEmpty() && category.isNotEmpty() && amount != null) {
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val geocoder = Geocoder(requireContext())
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                val cityName = addresses[0]?.locality
                                val address = cityName?.let { "$it, ${addresses[0]?.countryName}" } ?: "" // Contoh: Format alamat sebagai kota, negara

                                val repository = TransactionRepository(TransactionDatabase.getDatabase(requireContext()))
                                val viewModelFactory = TransactionViewModel.provideFactory(repository)
                                val transactionViewModel = ViewModelProvider(this, viewModelFactory).get(TransactionViewModel::class.java)

                                val transaction = TransactionEntity(
                                    0,
                                    title,
                                    category,
                                    amount.toInt(),
                                    address,
                                    Date()
                                )

                                transactionViewModel.insertTransaction(transaction)

                                // Reset input fields
                                binding.editTextJudul.text.clear()
                                binding.editTextNominal.text.clear()

                                Toast.makeText(requireContext(), "Transaction added successfully", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.navigation_transaction)
                            } else {
                                Toast.makeText(requireContext(), "Failed to get address", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to get location", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to get location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                requestLocationPermission()
            }
        } else {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}