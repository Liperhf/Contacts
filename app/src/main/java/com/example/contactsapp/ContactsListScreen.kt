package com.example.contactsapp

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ContactsListScreen() {
    val viewModel: ContactsViewModel = viewModel()
    val contacts by remember { derivedStateOf { viewModel.contacts } }
    val status = viewModel.status
    var searchText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(status.value) {
        status.value?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearStatus()
            }
        }
    }

    LaunchedEffect(searchText) {
        delay(300)
        viewModel.loadContacts(searchText)
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        viewModel.bindService()
        onDispose { viewModel.unbindService() }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(48.dp)),
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Search for contacts") }
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { viewModel.deleteDuplicates() },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                ) { Text(text = "Delete duplicates contacts") }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValue ->
        Box {
            val groupedContacts = viewModel.getGroupedContacts()
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValue)
            ) {
                groupedContacts.forEach { (letter, contacts) ->
                    item {
                        Text(
                            text = letter.toString(),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    items(contacts) { contact ->
                        ContactItem(contact)
                    }
                }
            }
        }
    }
}
