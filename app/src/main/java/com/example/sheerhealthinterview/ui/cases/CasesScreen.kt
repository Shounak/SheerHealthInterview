package com.example.sheerhealthinterview.ui.cases

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.Case
import com.example.sheerhealthinterview.ui.ActionLoadingState
import com.example.sheerhealthinterview.ui.ConfirmDeleteDialog
import com.example.sheerhealthinterview.ui.ErrorState
import com.example.sheerhealthinterview.ui.LoadingState
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.example.sheerhealthinterview.network.CaseStatus
import com.example.sheerhealthinterview.network.SheerAPI
import com.example.sheerhealthinterview.ui.theme.Purple40
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Composable
fun CasesScreen(
    caseClickedAction: (String) -> Unit,
    errorAction: suspend (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
    val casesViewModel: CasesViewModel = ViewModelProvider(
        viewModelStoreOwner,
        CasesViewModelFactory(SheerAPI.retrofitService)
    ).get(CasesViewModel::class.java)

    val casesUiState by casesViewModel.uiState.collectAsStateWithLifecycle()
    val casesActionState by casesViewModel.actionState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var caseIdToBeDeleted by rememberSaveable { mutableStateOf("") }
    var showCreateCaseDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(R.string.create_case)) },
                icon = { Icon(Icons.Filled.Edit, stringResource(R.string.create_case)) },
                onClick = {
                    showCreateCaseDialog = true
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

        if (showCreateCaseDialog) {
            NewItemDialog(
                dismissAction = { showCreateCaseDialog = false },
                confirmAction = { newCase ->
                    casesViewModel.createCase(newCase.first, newCase.second)
                    showCreateCaseDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CasesList(
    casesList: List<Case>,
    clickAction: (String) -> Unit,
    deleteAction: (String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    var searchText by rememberSaveable { mutableStateOf("") }
    val visibleItems = rememberSaveable { mutableStateOf(casesList) }

    LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
        stickyHeader {
            TextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    visibleItems.value = casesList.filter { case ->
                        case.title.contains(searchText, ignoreCase = true)
                    }
                },
                placeholder = {
                    Text(stringResource(R.string.search_cases))
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_cases)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            LongPressMessage()
        }
        items(visibleItems.value.size) { index ->
            val currentCase = visibleItems.value[index]
            CaseCard(
                currentCase,
                clickAction,
                deleteAction,
                Modifier.padding(10.dp)
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
                .padding(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = Purple40
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.long_press_message),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.White
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
                        contentDescription = stringResource(R.string.long_press_message_hide),
                        tint = Color.White
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
        update = { view ->
            val textView = view.findViewById<TextView>(R.id.case_title)
            val iconView = view.findViewById<ImageView>(R.id.status_icon)
            textView.text = case.title
            iconView.setImageResource(case.status.icon)
        },
        onReset = { view ->
            val textView = view.findViewById<TextView>(R.id.case_title)
            val iconView = view.findViewById<ImageView>(R.id.status_icon)
            textView.text = null
            iconView.setImageResource(R.drawable.round_person_24)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewItemDialog(
    dismissAction: () -> Unit,
    confirmAction: (Pair<String, CaseStatus>) -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(CaseStatus.WAITING_ON_TEAM) }
    val focusRequester = FocusRequester()

    AlertDialog(
        title = {
            Text(text = stringResource(R.string.create_case))
        },
        onDismissRequest = {
            dismissAction()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmAction(Pair(input, selectedStatus))
                }
            ) {
                Text(stringResource(R.string.create_case))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    dismissAction()
                }
            ) {
                Text(stringResource(R.string.dismiss))
            }
        },
        text = {
            Column {
                TextField(
                    modifier = Modifier.scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta -> delta })
                        .padding(bottom = 20.dp)
                        .focusRequester(focusRequester),
                    value = input,
                    placeholder = {
                        Text(stringResource(R.string.new_case_title))
                    },
                    onValueChange = {
                        input = it
                    }
                )
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }) {
                    TextField(
                        value = selectedStatus.title,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        CaseStatus.entries.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(text = status.title) },
                                onClick = {
                                    selectedStatus = status
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
