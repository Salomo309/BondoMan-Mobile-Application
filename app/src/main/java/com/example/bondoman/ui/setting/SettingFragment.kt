package com.example.bondoman.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bondoman.LoginActivity
import com.example.bondoman.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingViewModel = ViewModelProvider(this)[SettingViewModel::class.java]

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
            // Stop all services
            settingViewModel.logout()

            // Redirect to LoginActivity
            val activityIntent = Intent(requireContext(), LoginActivity::class.java)
            activityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(activityIntent)
            requireActivity().finish()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
