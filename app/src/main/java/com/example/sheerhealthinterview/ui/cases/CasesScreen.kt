package com.example.sheerhealthinterview.ui.cases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState

@Composable
fun CasesScreen(
    caseClickedAction: (String) -> Unit,
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
            CasesList((casesUiState as CasesUiState.Success).cases, caseClickedAction)
        }
    }
}

@Composable
private fun CasesList(casesList: List<Case>, clickAction: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier =  modifier.fillMaxSize()) {
        items(casesList.size) { index ->
            CaseCard(casesList[index], clickAction)
        }
    }
}

@Composable
private fun CaseCard(case: Case, clickAction: (String) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable {
            clickAction(case.caseId)
        }) {
        Text(case.title)
        Text(case.status.value)
    }
}