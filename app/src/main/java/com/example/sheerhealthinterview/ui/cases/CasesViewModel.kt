package com.example.sheerhealthinterview.ui.cases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.network.SheerAPI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface CasesUiState {
    data class Success(val cases: List<Case>): CasesUiState
    data object Loading: CasesUiState
    data object Error: CasesUiState
}

class CasesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CasesUiState>(CasesUiState.Loading)
    val uiState: StateFlow<CasesUiState> = _uiState.asStateFlow()

    init {
        getCases()
    }

    fun getCases() {
        _uiState.update { CasesUiState.Loading }

        viewModelScope.launch {
            val newUiState = try {
                val response = SheerAPI.retrofitService.getCases()
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    CasesUiState.Success(responseBody)
                } else {
                    CasesUiState.Error
                }
            } catch (exception: IOException) {
                CasesUiState.Error
            } catch (exception: HttpException) {
                CasesUiState.Error
            }

            _uiState.update { newUiState }
        }
    }
}