package com.example.sheerhealthinterview.ui.cases

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RampRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.AppScreen
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState
import com.example.sheerhealthinterview.ui.details.DetailsActionState
import kotlinx.coroutines.launch

@Composable
fun CasesScreen(
    caseClickedAction: (String) -> Unit,
    errorAction: suspend (Int) -> Unit,
    modifier: Modifier = Modifier,
    casesViewModel: CasesViewModel = viewModel()
) {
    val casesUiState by casesViewModel.uiState.collectAsStateWithLifecycle()
    val casesActionState by casesViewModel.actionState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.create_case)) },
                icon = { Icon(Icons.Filled.Edit, stringResource(R.string.create_case)) },
                onClick = {  })
        }
    ) { innerPadding ->
        when (casesUiState) {
            CasesUiState.Error -> {
                ErrorState(
                    retryAction = { casesViewModel.getCases() },
                    modifier = modifier.padding(innerPadding)
                )
            }

            CasesUiState.Loading -> {
                LoadingState(modifier.padding(innerPadding))
            }

            is CasesUiState.Success -> {
                CasesList(
                    (casesUiState as CasesUiState.Success).cases,
                    caseClickedAction,
                    modifier.padding(innerPadding)
                )
            }
        }

        when (casesActionState) {
            is CasesActionState.Error -> {
                val errorMessage = (casesActionState as CasesActionState.Error).errorMessage
                LaunchedEffect(Unit) {
                    coroutineScope.launch {
                        errorAction(errorMessage)
                    }
                }
            }
            CasesActionState.Loading -> TODO()
            is CasesActionState.Success -> TODO()
        }
    }
}

@Composable
private fun CasesList(
    casesList: List<Case>,
    clickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(casesList.size) { index ->
            CaseCard(
                casesList[index],
                clickAction,
                Modifier.padding(top = 20.dp, start = 10.dp, end = 10.dp)
            )
        }
    }
}

@SuppressLint("InflateParams")
@Composable
private fun CaseCard(case: Case, clickAction: (String) -> Unit, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.case_cell, null, false)
            val textView = view.findViewById<TextView>(R.id.case_title)
            textView.text = case.title
            val iconView = view.findViewById<ImageView>(R.id.status_icon)
            iconView.setImageResource(case.status.icon)

            view // return the view
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { clickAction(case.caseId) }
    )
}

