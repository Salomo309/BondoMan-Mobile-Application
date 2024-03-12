package com.example.bondoman

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.bondoman.R

class TxnActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_transaction)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner = findViewById<Spinner>(R.id.editCategory)
        spinner.adapter = adapter
    }
}