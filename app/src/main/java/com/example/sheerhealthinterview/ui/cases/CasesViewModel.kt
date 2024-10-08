package com.example.sheerhealthinterview.ui.cases

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.network.CaseStatus
import com.example.sheerhealthinterview.network.NewCase
import com.example.sheerhealthinterview.network.SheerApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface CasesUiState {
    data class Success(val cases: MutableList<Case>): CasesUiState
    data object Loading: CasesUiState
    data object Error: CasesUiState
}

sealed interface CasesActionState {
    data class Success(val scrollToBottom: Boolean) : CasesActionState
    data object Loading : CasesActionState
    data class Error(@StringRes val errorMessage: Int) : CasesActionState
}

class CasesViewModelFactory(private val apiService: SheerApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CasesViewModel::class.java)) {
            return CasesViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CasesViewModel(private val apiService: SheerApiService) : ViewModel() {
    private val _uiState = MutableStateFlow<CasesUiState>(CasesUiState.Loading)
    val uiState: StateFlow<CasesUiState> = _uiState.asStateFlow()

    private val _actionState =
        MutableStateFlow<CasesActionState>(CasesActionState.Success(false))
    val actionState: StateFlow<CasesActionState> = _actionState.asStateFlow()

    init {
        getCases()
    }

    fun getCases() {
        _uiState.update { CasesUiState.Loading }

        viewModelScope.launch {
            val newUiState = try {
                val response = apiService.getCases()
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

    fun createCase(caseTitle: String, caseStatus: CaseStatus) {
        _actionState.update { CasesActionState.Loading }

        viewModelScope.launch {
            val newActionState = try {
                val response = apiService.createCase(
                    NewCase(caseTitle, caseStatus)
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (uiState.value is CasesUiState.Success && responseBody != null) {
                        val currentMessagesList = (uiState.value as CasesUiState.Success).cases
                        currentMessagesList.add(responseBody)
                        _uiState.update { CasesUiState.Success(currentMessagesList) }
                    }
                    CasesActionState.Success(true)
                } else {
                    CasesActionState.Error(R.string.create_case_error)
                }
            } catch (exception: IOException) {
                CasesActionState.Error(R.string.create_case_error)
            } catch (exception: HttpException) {
                CasesActionState.Error(R.string.create_case_error)
            }

            _actionState.update { newActionState }
        }
    }

    fun deleteCase(caseId: String) {
        _actionState.update { CasesActionState.Loading }

        viewModelScope.launch {
            val newActionState = try {
                val response = apiService.deleteCase(caseId)
                if (response.isSuccessful) {
                    if (uiState.value is CasesUiState.Success) {
                        val currentCasesList = (uiState.value as CasesUiState.Success).cases
                        currentCasesList.removeIf { it.caseId == caseId }
                        _uiState.update { CasesUiState.Success(currentCasesList) }
                    }
                    CasesActionState.Success(false)
                } else {
                    CasesActionState.Error(R.string.delete_case_error)
                }
            } catch (exception: IOException) {
                CasesActionState.Error(R.string.delete_case_error)
            } catch (exception: HttpException) {
                CasesActionState.Error(R.string.delete_case_error)
            }

            _actionState.update { newActionState }
        }
    }
}