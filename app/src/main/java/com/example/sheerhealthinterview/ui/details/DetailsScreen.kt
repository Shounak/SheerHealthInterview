package com.example.sheerhealthinterview.ui.details

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.network.CaseDetails
import com.example.sheerhealthinterview.network.Detail
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState

@Composable
fun DetailsScreen(
    caseId: String,
    modifier: Modifier = Modifier
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    val detailsViewModel: DetailsViewModel = ViewModelProvider(
        viewModelStoreOwner,
        DetailsViewModelFactory(caseId)
    ).get(DetailsViewModel::class.java)

    val detailsUiState by detailsViewModel.uiState.collectAsStateWithLifecycle()

    when (detailsUiState) {
        DetailsUiState.Error -> {
            ErrorState(retryAction = { detailsViewModel.getDetails(caseId) }, modifier = modifier)
        }

        DetailsUiState.Loading -> {
            LoadingState(modifier)
        }

        is DetailsUiState.Success -> {
            CaseDetails((detailsUiState as DetailsUiState.Success).details)
        }
    }
}

@Composable
private fun CaseDetails(caseDetails: CaseDetails, modifier: Modifier = Modifier) {
    LazyColumn(modifier =  modifier.fillMaxSize()) {

        items(caseDetails.details.size) { index ->
            ChatCard(caseDetails.details[index], {  })
        }
    }
}

@Composable
private fun ChatCard(chat: Detail, clickAction: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier
        .fillMaxWidth()
        .padding(10.dp)
        .clickable {
            clickAction()
        }) {
        Row {

        }

        Text(chat.message)
        Text(chat.direction.name)
    }
}