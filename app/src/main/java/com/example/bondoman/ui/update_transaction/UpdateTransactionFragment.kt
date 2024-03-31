package com.example.bondoman.ui.update_transaction

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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

        currentTransaction = arguments?.getParcelable<Transaction>("transaction")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateTransactionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instantiate Transaction View Model
        val transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Populate EditText fields with transaction data
        currentTransaction?.let {
            binding.editTextJudul.setText(it.title)
            binding.editTextNominal.setText(it.amount.toString())
            binding.editTextLokasi.setText(it.location)
        }

        // Update Button Click Listener
        binding.buttonUpdate.setOnClickListener {
            updateTransaction(transactionViewModel)

            findNavController().navigate(R.id.navigation_transaction)
        }

    }

    companion object {
        private const val ARG_TRANSACTION = "transaction"

        fun newInstance(transaction: Transaction): UpdateTransactionFragment {
            val fragment = UpdateTransactionFragment()
            val args = Bundle().apply {
                putParcelable(ARG_TRANSACTION, transaction)
            }
            fragment.arguments = args
            return fragment
        }
    }

    fun updateTransaction(transactionViewModel: TransactionViewModel) {
        currentTransaction?.let { transaction ->
            val newTitle = binding.editTextJudul.text.toString()
            val newAmount = binding.editTextNominal.text.toString().toDoubleOrNull() ?: transaction.amount
            val newLocation = binding.editTextLokasi.text.toString()

            // Update title, amount, and location of currentTransaction
            transaction.title = newTitle
            transaction.amount = newAmount
            transaction.location = newLocation

            getLongLat(requireContext(), newLocation, transaction.latitude, transaction.longitude)?.let { (latitude, longitude) ->

                // Update latitude and longitude of currentTransaction
                transaction.latitude = latitude
                transaction.longitude = longitude

                // Update the transaction in the ViewModel
                transactionViewModel.updateTransaction(toTransactionEntity(transaction))
            }
        }
    }

    fun toTransactionEntity(transaction: Transaction) : TransactionEntity {
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

    fun getLongLat(context: Context, location: String, oldLatitude: Double, oldLongitude: Double): Pair<Double, Double>? {
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
                return Pair(oldLatitude, oldLongitude)
            }
        } catch (e: IOException) {
            Log.e("Location", "Error getting location from Geocoder: ${e.message}")
            return null
        }
    }
}