package com.example.sheerhealthinterview.ui.cases

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState

@Composable
fun CasesScreen(
    modifier: Modifier = Modifier,
    casesViewModel: CasesViewModel = viewModel()
) {
    val casesUiState by casesViewModel.uiState.collectAsStateWithLifecycle()

    when (casesUiState) {
        CasesUiState.Error -> {
            ErrorState(retryAction = { casesViewModel.getCases() }, modifier = modifier)
        }

        CasesUiState.Loading -> {
            LoadingState(modifier)
        }

        is CasesUiState.Success -> {
            CasesList((casesUiState as CasesUiState.Success).cases)
        }
    }
}

@Composable
fun CasesList(casesList: List<Case>, modifier: Modifier = Modifier) {
    Column(modifier =  modifier) {
        for (case in casesList) {
            Text(case.title)
        }
    }
}