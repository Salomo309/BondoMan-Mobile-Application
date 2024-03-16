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


class AddTransactionFragment : Fragment() {
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!

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
        val transactionViewModel =
            ViewModelProvider(this)[AddTransactionViewModel::class.java]

        _binding = FragmentAddTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.editCategory.adapter = adapter

        checkLocationPermission()

        return root
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Izin lokasi diberikan.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Izin lokasi ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}