package com.example.sheerhealthinterview.ui.cases

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.ui.ActionLoadingState
import com.example.sheerhealthinterview.ui.ConfirmDeleteDialog
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState
import com.example.sheerhealthinterview.ui.NewItemDialog
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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
    val listState = rememberLazyListState()
    var caseIdToBeDeleted by rememberSaveable { mutableStateOf("") }
    var showCrateCaseDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.create_case)) },
                icon = { Icon(Icons.Filled.Edit, stringResource(R.string.create_case)) },
                onClick = {
                    showCrateCaseDialog = true
                })
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
                    { caseId ->
                        caseIdToBeDeleted = caseId
                    },
                    listState,
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

            CasesActionState.Loading -> {
                ActionLoadingState()
            }

            is CasesActionState.Success -> {
                if ((casesActionState as CasesActionState.Success).scrollToBottom) {
                    LaunchedEffect(Unit) {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
                        }
                    }
                }
            }
        }

        if (caseIdToBeDeleted.isNotEmpty()) {
            ConfirmDeleteDialog(
                dismissAction = { caseIdToBeDeleted = "" },
                confirmAction = {
                    casesViewModel.deleteCase(caseIdToBeDeleted)
                    caseIdToBeDeleted = ""
                },
                dialogTitle = stringResource(R.string.delete_case_confirm)
            )
        }

        if (showCrateCaseDialog) {
            NewItemDialog(
                dismissAction = { showCrateCaseDialog = false },
                confirmAction = { caseTitle ->
                    casesViewModel.createCase(caseTitle)
                    showCrateCaseDialog = false
                },
                dialogTitle = stringResource(R.string.create_case)
            )
        }
    }
}

@Composable
private fun CasesList(
    casesList: List<Case>,
    clickAction: (String) -> Unit,
    deleteAction: (String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        item {
            LongPressMessage()
        }
        items(casesList.size) { index ->
            val currentCase = casesList[index]
            CaseCard(
                currentCase,
                clickAction,
                deleteAction,
                Modifier.padding(top = 20.dp, start = 10.dp, end = 10.dp)
            )
        }
    }
}

@Composable
fun LongPressMessage(modifier: Modifier = Modifier) {
    val datastore = LocalContext.current.dataStore
    val coroutineScope = rememberCoroutineScope()
    val SHOW_LONG_PRESS_MESSAGE = intPreferencesKey("SHOW_LONG_PRESS_MESSAGE")

    /**
     * Datastore works best with Ints
     * 0 to show message
     * 1 to not show message
     */
    val showMessageValueFlow: Flow<Int> = datastore.data
        .map { preferences ->
            preferences[SHOW_LONG_PRESS_MESSAGE] ?: 0
        }
    val showMessage by showMessageValueFlow.collectAsStateWithLifecycle(1)

    if (showMessage == 0) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.long_press_message),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = {
                    coroutineScope.launch {
                        datastore.edit { preferences ->
                            preferences[SHOW_LONG_PRESS_MESSAGE] = 1
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.long_press_message_hide)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("InflateParams")
@Composable
private fun CaseCard(
    case: Case,
    clickAction: (String) -> Unit,
    deleteAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

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
            .combinedClickable(
                onClick = { clickAction(case.caseId) },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    deleteAction(case.caseId)
                },
                onLongClickLabel = stringResource(R.string.delete_case_confirm)
            )
    )
}
