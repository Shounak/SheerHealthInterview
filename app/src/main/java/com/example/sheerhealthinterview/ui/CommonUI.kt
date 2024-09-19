package com.example.sheerhealthinterview.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.sheerhealthinterview.R
import com.example.sheerhealthinterview.ui.theme.Purple40

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.width(64.dp), color = Purple40)
    }
}

@Composable
fun ErrorState(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error")
        Button(
            onClick = retryAction,
            modifier = modifier.padding(top = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Purple40,
                contentColor = Color.White
            )
        ) {
            Text(text = "Retry")
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    dismissAction: () -> Unit,
    confirmAction: () -> Unit,
    dialogTitle: String,
) {
    AlertDialog(
        title = {
            Text(text = dialogTitle)
        },
        onDismissRequest = {
            dismissAction()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    confirmAction()
                }
            ) {
                Text(stringResource(R.string.confirm))
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
        }
    )
}

@Composable
fun ActionLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = Purple40
        )
    }
}
