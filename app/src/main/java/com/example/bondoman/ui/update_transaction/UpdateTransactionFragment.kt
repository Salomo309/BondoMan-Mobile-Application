package com.example.bondoman.ui.update_transaction

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bondoman.MainActivity
import com.example.bondoman.R
import com.example.bondoman.databinding.FragmentUpdateTransactionBinding
import com.example.bondoman.models.Transaction
import com.example.bondoman.room.TransactionEntity
import com.example.bondoman.ui.transaction.TransactionViewModel
import java.io.IOException

class UpdateTransactionFragment : Fragment() {
    private var currentTransaction: Transaction? = null
    private var _binding: FragmentUpdateTransactionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentTransaction = arguments?.getParcelable("transaction")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateTransactionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate Transaction View Model
        val transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Populate EditText fields with transaction data
        currentTransaction?.let {
            binding.modifyTextJudul.setText(it.title)
            binding.modifyTextNominal.setText(it.amount.toString())
            binding.editTextLokasi.setText(it.location)
        }

        // Update Button Click Listener
        binding.buttonUpdate.setOnClickListener {
            updateTransaction(transactionViewModel)

            findNavController().navigate(R.id.navigation_transaction)
        }

    }

    private fun updateTransaction(transactionViewModel: TransactionViewModel) {
        currentTransaction?.let { transaction ->
            val newTitle = binding.modifyTextJudul.text.toString()
            val newAmount = binding.modifyTextNominal.text.toString().toDoubleOrNull() ?: transaction.amount
            val newLocation = binding.editTextLokasi.text.toString()

            // Update title, amount, and location of currentTransaction
            transaction.title = newTitle
            transaction.amount = newAmount

            getLongLat(requireContext(), newLocation, transaction.latitude, transaction.longitude)?.let { (latitude, longitude) ->

                if (transaction.latitude != latitude || transaction.longitude != longitude) {
                    // Update latitude and longitude of currentTransaction
                    transaction.latitude = latitude
                    transaction.longitude = longitude
                    transaction.location = newLocation
                }

                // Update the transaction in the ViewModel
                transactionViewModel.updateTransaction(toTransactionEntity(transaction))
            }
        }
    }

    private fun toTransactionEntity(transaction: Transaction) : TransactionEntity {
        return TransactionEntity(
            id = transaction.id,
            nim = transaction.nim,
            title = transaction.title,
            category = transaction.category,
            amount = transaction.amount,
            location = transaction.location,
            longitude = transaction.longitude,
            latitude = transaction.latitude,
            date = transaction.date
        )
    }

    private fun getLongLat(context: Context, location: String, oldLatitude: Double, oldLongitude: Double): Pair<Double, Double>? {
        val geocoder = Geocoder(context)
        return try {
            val addresses = geocoder.getFromLocationName(location, 1)
            if (!addresses.isNullOrEmpty()) {
                val latitude = addresses[0].latitude
                val longitude = addresses[0].longitude
                Pair(latitude, longitude)
            } else {
                Log.e("Location", "No address found for the location: $location")
                Pair(oldLatitude, oldLongitude)
            }
        } catch (e: IOException) {
            Log.e("Location", "Error getting location from Geocoder: ${e.message}")
            null
        }
    }
}
