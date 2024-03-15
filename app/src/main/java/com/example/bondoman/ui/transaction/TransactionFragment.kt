package com.example.bondoman.ui.transaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bondoman.databinding.FragmentTransactionBinding
import com.example.bondoman.R
import com.example.bondoman.models.Transaction
import java.util.Date


class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_transaction)
        }

        var transactionList = mutableListOf(
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),
            Transaction(1,"Transaction", "Pemasukan", 100000, "Bandung", Date()),


        )

        val rvTransactions = binding.rvTransactions
        val adapter = TransactionAdapter(transactionList)
        rvTransactions.adapter = adapter
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}