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
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.transaction.TransactionViewModel
import java.util.Date
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.bondoman.MainActivity


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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Transaction View Model
        val transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Check Location Permission
        if (!checkLocationPermission()) {
            requestLocationPermission()
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

    private fun addTransaction(transactionViewModel: TransactionViewModel) {
        val title = binding.editTextJudul.text.toString()
        val category = binding.editCategory.selectedItem.toString()
        val amount = binding.editTextNominal.text.toString().toDoubleOrNull()

        // Check Fields
        if (title.isNotEmpty() && category.isNotEmpty() && amount != null) {

            // Check location permission
            if (checkLocationPermission()) {

                // Get Location
                getDeviceLocation { latitude, longitude, address ->

                    // Set Transaction ID
                    var id = 1
                    if (transactionViewModel.listTransactions.value != null) {
                        id = transactionViewModel.listTransactions.value!!.size + 1
                    }

                    // Form new TransactionEntity Object
                    val transaction = TransactionEntity(
                        id.toLong(),
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
                // requestLocationPermission()

                // Get Last Location
                getDeviceLocation { latitude, longitude, address ->

                    // Set Transaction ID
                    var id = 1
                    if (transactionViewModel.listTransactions.value != null) {
                        id = transactionViewModel.listTransactions.value!!.size + 1
                    }

                    // Form new TransactionEntity Object
                    val transaction = TransactionEntity(
                        id.toLong(),
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
        } else {
            Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceLocation(callback: (Double, Double, String) -> Unit) {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(requireContext())
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {

                            // Get location
                            val address = addresses[0] // Mengambil objek Address pertama dari daftar alamat

                            // val admin = address.adminArea // Provinsi
                            val subAdmin = address.subAdminArea // Kota
                            val locality = address.locality // Kecamatan
                             val thoroughfare = address.thoroughfare // Jalan

                            val addr: String = "$thoroughfare, $locality, $subAdmin"

                            // Call callback with obtained values
                            callback.invoke(location.latitude, location.longitude, addr)
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