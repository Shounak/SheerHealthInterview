package com.example.sheerhealthinterview.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sheerhealthinterview.network.CaseDetails
import com.example.sheerhealthinterview.network.SheerAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface DetailsUiState {
    data class Success(val details: CaseDetails): DetailsUiState
    data object Loading: DetailsUiState
    data object Error: DetailsUiState
}

class DetailsViewModelFactory(private val caseId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(caseId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DetailsViewModel(caseId: String) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        getDetails(caseId)
    }

    fun getDetails(caseId: String) {
        _uiState.update { DetailsUiState.Loading }

        viewModelScope.launch {
            val newUiState = try {
                val response = SheerAPI.retrofitService.getDetails(caseId)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    DetailsUiState.Success(responseBody)
                } else {
                    DetailsUiState.Error
                }
            } catch (exception: IOException) {
                DetailsUiState.Error
            } catch (exception: HttpException) {
                DetailsUiState.Error
            }

            _uiState.update { newUiState }
        }
    }

    fun deleteMessage(caseId: String, detailId: String) {
        viewModelScope.launch {
            try {
                val response = SheerAPI.retrofitService.deleteDetail(caseId, detailId)
                if (response.isSuccessful) {
                    getDetails(caseId)
                } else {
                    val x = 0
                }
            } catch (exception: IOException) {
                val x = 0
            } catch (exception: HttpException) {
                val x = 0
            }
        }
    }
}


