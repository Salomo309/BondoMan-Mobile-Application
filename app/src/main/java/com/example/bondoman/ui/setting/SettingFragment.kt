package com.example.bondoman.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // val settingViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.buttonSaveTransaction.setOnClickListener {
            // Handle save transaction button click
            Toast.makeText(requireContext(), "Save Transaction Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSendTransaction.setOnClickListener {
            // Handle send transaction button click
            Toast.makeText(requireContext(), "Send Transaction Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.buttonLogout.setOnClickListener {
            // Handle logout button click
            Toast.makeText(requireContext(), "Logout Clicked", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
