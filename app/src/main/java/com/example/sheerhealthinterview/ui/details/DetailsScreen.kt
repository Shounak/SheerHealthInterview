package com.example.sheerhealthinterview.ui.details

import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.network.CaseDetails
import com.example.sheerhealthinterview.network.ChatDirection
import com.example.sheerhealthinterview.network.Detail
import com.example.sheerhealthinterview.ui.ConfirmDeleteDialog
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
    var detailIdToBeDeleted by remember { mutableStateOf("") }

    when (detailsUiState) {
        DetailsUiState.Error -> {
            ErrorState(retryAction = { detailsViewModel.getDetails(caseId) }, modifier = modifier)
        }

        DetailsUiState.Loading -> {
            LoadingState(modifier)
        }

        is DetailsUiState.Success -> {
            CaseDetails(
                (detailsUiState as DetailsUiState.Success).details,
                { detailId ->
                    detailIdToBeDeleted = detailId
                }
            )
        }
    }

    if (detailIdToBeDeleted.isNotEmpty()) {
        ConfirmDeleteDialog(
            onDismissRequest = { detailIdToBeDeleted = "" },
            onConfirmation = {
                detailsViewModel.deleteMessage(caseId, detailIdToBeDeleted)
                detailIdToBeDeleted = ""
            },
            dialogTitle = stringResource(R.string.delete_message_confirm)
        )
    }
}

@Composable
private fun CaseDetails(
    caseDetails: CaseDetails,
    deleteAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                reverseLayout = true
            ) {
                items(caseDetails.details.size) { index ->
                    ChatCard(
                        caseDetails.details[index],
                        deleteAction,
                        Modifier.padding(top = 20.dp, start = 10.dp, end = 10.dp)
                    )
                }
            }
        }

        ComposeMessageBar()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatCard(chat: Detail, deleteAction: (String) -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current

    when (chat.direction) {
        ChatDirection.TEAM -> {
            AndroidView(
                factory = { context ->
                    val view =
                        LayoutInflater.from(context)
                            .inflate(R.layout.team_message_cell, null, false)
                    val textView = view.findViewById<TextView>(R.id.message_text)
                    textView.text = chat.message

                    view // return the view
                },
                modifier = modifier
                    .padding(end = 50.dp)
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            deleteAction(chat.detailId)
                        },
                        onLongClickLabel = stringResource(R.string.delete_message)
                    )
            )
        }

        ChatDirection.USER -> {
            AndroidView(
                factory = { context ->
                    val view =
                        LayoutInflater.from(context)
                            .inflate(R.layout.user_message_cell, null, false)
                    val textView = view.findViewById<TextView>(R.id.message_text)
                    textView.text = chat.message

                    view // return the view
                },
                modifier = modifier
                    .padding(start = 50.dp)
                    .fillMaxWidth()
                    .clickable { deleteAction(chat.detailId) }
            )
        }
    }
}

@Composable
private fun ComposeMessageBar(modifier: Modifier = Modifier) {
    var messageText by rememberSaveable { mutableStateOf("") }

    Row(
        modifier = modifier.padding(top = 20.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Spacer(modifier = Modifier.width(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.LightGray, CircleShape),
        ) {
            TextField(
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta -> delta }
                    ),
                value = messageText,
                placeholder = {
                    Text(stringResource(R.string.text_message))
                },
                onValueChange = {
                    messageText = it
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    disabledTextColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            IconButton(
                onClick = {
                    // send message and refresh screen
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = stringResource(R.string.send_message)
                )
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
    }
    Spacer(modifier = Modifier.height(10.dp))
}