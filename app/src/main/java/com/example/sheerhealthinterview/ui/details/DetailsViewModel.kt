package com.example.sheerhealthinterview.ui.details

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.ChatDirection
import com.example.sheerhealthinterview.network.Detail
import com.example.sheerhealthinterview.network.NewDetail
import com.example.sheerhealthinterview.network.SheerApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface DetailsUiState {
    data class Success(val details: MutableList<Detail>) : DetailsUiState
    data object Loading : DetailsUiState
    data object Error : DetailsUiState
}

sealed interface DetailsActionState {
    data class Success(val scrollToBottom: Boolean) : DetailsActionState
    data object Loading : DetailsActionState
    data class Error(@StringRes val errorMessage: Int) : DetailsActionState
}

class DetailsViewModelFactory(private val caseId: String, private val apiService: SheerApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(caseId, apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class DetailsViewModel(caseId: String, private val apiService: SheerApiService) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    private val _actionState =
        MutableStateFlow<DetailsActionState>(DetailsActionState.Success(false))
    val actionState: StateFlow<DetailsActionState> = _actionState.asStateFlow()

    init {
        getDetails(caseId)
    }

    fun getDetails(caseId: String) {
        _uiState.update { DetailsUiState.Loading }

        viewModelScope.launch {
            val newUiState = try {
                val response = apiService.getDetails(caseId)
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    DetailsUiState.Success(responseBody.details)
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
        _actionState.update { DetailsActionState.Loading }

        viewModelScope.launch {
            val newActionState = try {
                val response = apiService.deleteDetail(caseId, detailId)
                if (response.isSuccessful) {
                    if (uiState.value is DetailsUiState.Success) {
                        val currentMessagesList = (uiState.value as DetailsUiState.Success).details
                        currentMessagesList.removeIf { it.detailId == detailId }
                        _uiState.update { DetailsUiState.Success(currentMessagesList) }
                    }
                    DetailsActionState.Success(false)
                } else {
                    DetailsActionState.Error(R.string.delete_message_error)
                }
            } catch (exception: IOException) {
                DetailsActionState.Error(R.string.delete_message_error)
            } catch (exception: HttpException) {
                DetailsActionState.Error(R.string.delete_message_error)
            }

            _actionState.update { newActionState }
        }
    }

    fun sendMessage(textMessage: String, caseId: String) {
        _actionState.update { DetailsActionState.Loading }

        viewModelScope.launch {
            val newActionState = try {
                val response = apiService.createDetail(
                    caseId,
                    NewDetail(textMessage, ChatDirection.USER)
                )
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (uiState.value is DetailsUiState.Success && responseBody != null) {
                        val currentMessagesList = (uiState.value as DetailsUiState.Success).details
                        currentMessagesList.add(responseBody)
                        _uiState.update { DetailsUiState.Success(currentMessagesList) }
                    }
                    DetailsActionState.Success(true)
                } else {
                    DetailsActionState.Error(R.string.create_message_error)
                }
            } catch (exception: IOException) {
                DetailsActionState.Error(R.string.create_message_error)
            } catch (exception: HttpException) {
                DetailsActionState.Error(R.string.create_message_error)
            }

            _actionState.update { newActionState }
        }
    }
}


