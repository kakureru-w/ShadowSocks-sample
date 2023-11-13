package com.ssvpntest.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.github.shadowsocks.database.Profile

data class MainScreenState(
    val keyField: String = "",
    val connected: Boolean = false,
    val profiles: List<Profile> = emptyList(),
)

@Composable
fun MainScreen(
    state: MainScreenState,
    onConnectionButtonClick: () -> Unit,
    onKeyValueChange: (String) -> Unit,
    onKeyDoneClick: () -> Unit,
    onProfileClick: (id: Long) -> Unit,
) {
    Scaffold(
        bottomBar = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = state.keyField, onValueChange = onKeyValueChange,
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        Text(text = "New key")
                    },
                    trailingIcon = {
                        IconButton(onClick = onKeyDoneClick) {
                            Icon(imageVector = Icons.Rounded.Done, contentDescription = null)
                        }
                    },
                )
                Button(
                    onClick = onConnectionButtonClick,
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = "Start / stop", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Profiles (click to activate):",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(items = state.profiles, key = { item -> item.id }) {
                Surface(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { onProfileClick(it.id) },
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "${it.host}:${it.remotePort}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}