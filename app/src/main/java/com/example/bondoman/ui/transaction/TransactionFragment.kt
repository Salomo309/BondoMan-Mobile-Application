package com.example.bondoman.ui.transaction

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bondoman.MainActivity
import com.example.bondoman.databinding.FragmentTransactionBinding
import com.example.bondoman.R
import com.example.bondoman.models.Transaction
import com.example.bondoman.room.TransactionEntity

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get Transaction View Model
        val transactionViewModel = (requireActivity() as MainActivity).getTransactionViewModel()

        // Add Button Navigate to Add Transaction Fragment
        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_add_transaction)
        }

        // Empty State
        val noTransactionTextView: TextView = root.findViewById(R.id.noTransactionTextView)
        val rvTransactions = binding.rvTransactions

        // Transaction Adapter
        transactionAdapter  = TransactionAdapter(emptyList(), transactionViewModel)
        rvTransactions.adapter = transactionAdapter
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())

        // Get Transaction List
        transactionViewModel.listTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            if (transactions.isEmpty()) {
                noTransactionTextView.visibility = View.VISIBLE
            } else {
                noTransactionTextView.visibility = View.GONE
                transactionAdapter.transactions = transactions.map { transactionEntityToModel(it) }
                transactionAdapter.notifyDataSetChanged()
            }
        })

        return root
    }

    private fun transactionEntityToModel(entity: TransactionEntity): Transaction {
        return Transaction(
            entity.id,
            entity.title,
            entity.nim,
            entity.category,
            entity.amount,
            entity.location,
            entity.longitude,
            entity.latitude,
            entity.date
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}