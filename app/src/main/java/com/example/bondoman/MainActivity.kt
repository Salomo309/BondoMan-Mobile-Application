package com.example.bondoman

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import kotlinx.coroutines.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bondoman.databinding.ActivityMainBinding
import com.example.bondoman.repository.TransactionRepository
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.service.NetworkStateService
import com.example.bondoman.storage.TokenManager
import com.example.bondoman.ui.transaction.TransactionViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionViewModel: TransactionViewModel

    // Boolean
    private var isConnected : Boolean = true
    private var isBroadcastEnabled = false

    // Randomized Title
    private var randomizedTitle: String? = null

    // Broadcast Receiver
    private lateinit var networkStateReceiver : BroadcastReceiver
    private lateinit var randomizerBroadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startNetworkStateService()
        setupNetworkStateReceiver()
        setupBroadcastReceiver()

        // Initialize ViewModel
        var repository: TransactionRepository
        var viewModelFactory: ViewModelProvider.Factory

        // Start a coroutine to fetch the nim
        CoroutineScope(Dispatchers.Main).launch {
            val nim = TokenManager.fetchUserNim(applicationContext)
            repository = nim?.let { TransactionRepository(TransactionDatabase.getDatabase(applicationContext), it) }!!
            viewModelFactory = TransactionViewModel.provideFactory(repository)
            transactionViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TransactionViewModel::class.java]

            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navView: BottomNavigationView = binding.navView

            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_transaction,
                    R.id.navigation_scan,
                    R.id.navigation_graph,
                    R.id.navigation_setting
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    fun getTransactionViewModel(): TransactionViewModel {
        return transactionViewModel
    }

    fun getIsConnected() : Boolean {
        return isConnected
    }

    fun getIsBroadcastEnabled(): Boolean {
        return isBroadcastEnabled
    }

    fun getRandomizedTitle(): String? {
        return randomizedTitle
    }

    fun setIsBroadcastEnabled(boolean: Boolean) {
        isBroadcastEnabled = boolean
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    public fun disableNavBar() {
        for (i in 0 until binding.navView.menu.size()) {
            val menuItem = binding.navView.menu.getItem(i)
            menuItem.isEnabled = false
        }
    }

    public fun enableNavBar() {
        for (i in 0 until binding.navView.menu.size()) {
            val menuItem = binding.navView.menu.getItem(i)
            menuItem.isEnabled = true
        }
    }

    private fun startNetworkStateService() {
        val intent = Intent(this, NetworkStateService::class.java)
        startService(intent)
    }

    private fun setupNetworkStateReceiver() {
        networkStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NetworkStateService.ACTION_NETWORK_STATE_CHANGE) {
                    isConnected = intent.getBooleanExtra(NetworkStateService.EXTRA_NETWORK_STATE, false)
                    if (!isConnected) {
                        val alertDialogBuilder = AlertDialog.Builder(context)
                        alertDialogBuilder.apply {
                            setTitle("No Internet Found")
                            setMessage("App functionalities will be limited")
                            setPositiveButton("I understand") { dialog, _ ->
                                dialog.dismiss()
                            }
                            setCancelable(true)
                        }
                        alertDialogBuilder.create().show()
                    }
                }
            }
        }
        val filter = IntentFilter(NetworkStateService.ACTION_NETWORK_STATE_CHANGE)
        ContextCompat.registerReceiver(this, networkStateReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun setupBroadcastReceiver() {
        randomizerBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.bondoman.RANDOM_TRANSACTION_ACTION") {
                    if (isBroadcastEnabled) {
                        // Receive intent
                        val title = intent?.getStringExtra("title")
                        if (title != null) {
                            randomizedTitle = title
                        }
                    }
                }
            }
        }
        val filter = IntentFilter("com.example.bondoman.RANDOM_TRANSACTION_ACTION")
        ContextCompat.registerReceiver(this, randomizerBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(randomizerBroadcastReceiver)
        this.unregisterReceiver(networkStateReceiver)
    }
}
