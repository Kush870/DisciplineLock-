package com.disciplinelock.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val limitMinutes by viewModel.dailyLimitMinutes.collectAsState()
    val isHardcore by viewModel.isHardcoreMode.collectAsState()
    val isLocked by viewModel.isLockedByHardcore.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            if (isLocked) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = "HARDCORE MODE ACTIVE. Settings are locked until tomorrow because you have already opened Instagram today.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "DAILY LIMIT",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodyMedium,
                letterSpacing = 2.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${limitMinutes}m",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = limitMinutes.toFloat(),
                onValueChange = { viewModel.updateDailyLimit(it.toInt()) },
                valueRange = 0f..120f,
                steps = 23, // 5 min increments
                enabled = !isLocked,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.onBackground,
                    activeTrackColor = MaterialTheme.colorScheme.onBackground,
                    inactiveTrackColor = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HARDCORE MODE",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Prevents changing limit after you open Instagram for the day.",
                        color = MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Switch(
                    checked = isHardcore,
                    onCheckedChange = { viewModel.toggleHardcoreMode(it) },
                    enabled = !isLocked,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        checkedTrackColor = MaterialTheme.colorScheme.onBackground,
                        uncheckedThumbColor = MaterialTheme.colorScheme.tertiary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}
