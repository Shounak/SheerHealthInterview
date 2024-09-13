package com.example.sheerhealthinterview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheerhealthinterview.network.SheerAPI
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class CasesViewModel : ViewModel() {
    init {
        getCases()
    }

    fun getCases() {
        viewModelScope.launch {
            try {
                val x = SheerAPI.retrofitService.getCases()
            } catch (exception: IOException) {

            } catch (exception: HttpException) {
                
            }
        }
    }
}