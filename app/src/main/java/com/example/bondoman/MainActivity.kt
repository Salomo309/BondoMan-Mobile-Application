package com.example.bondoman

import android.os.Bundle
import kotlinx.coroutines.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bondoman.databinding.ActivityMainBinding
import com.example.bondoman.repository.TransactionRepository
import com.example.bondoman.room.TransactionDatabase
import com.example.bondoman.storage.TokenManager
import com.example.bondoman.ui.transaction.TransactionViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var transactionViewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
}
