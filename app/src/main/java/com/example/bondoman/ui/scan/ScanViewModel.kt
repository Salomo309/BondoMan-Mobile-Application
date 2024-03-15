package com.example.bondoman.ui.scan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bondoman.models.Item

class ScanViewModel : ViewModel() {

    private val itemListLiveData = MutableLiveData<List<Item>>()

    fun setItemList(itemList: List<Item>) {
        itemListLiveData.value = itemList
    }

    fun getItemList(): LiveData<List<Item>> {
        return itemListLiveData
    }
}