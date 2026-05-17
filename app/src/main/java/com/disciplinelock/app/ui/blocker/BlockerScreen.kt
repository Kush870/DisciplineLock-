package com.disciplinelock.app.ui.blocker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BlockerScreen(
    viewModel: BlockerViewModel = hiltViewModel(),
    onEmergencyUnlock: () -> Unit,
    onGoBack: () -> Unit
) {
    val canEmergencyUnlock by viewModel.canUseEmergencyUnlock.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.onBackground) // Invert background for the blocker
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DISCIPLINE REQUIRED",
            color = MaterialTheme.colorScheme.background,
            style = MaterialTheme.typography.titleLarge,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "You have reached your daily Instagram limit.\n\nKeep the promise you made to yourself.",
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onGoBack,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground
            )
        ) {
            Text("GO BACK", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (canEmergencyUnlock) {
            Button(
                onClick = {
                    viewModel.useEmergencyUnlock()
                    onEmergencyUnlock()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text("EMERGENCY UNLOCK (5 MIN)", fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Warning: Using emergency unlock will decrease your Discipline Score.",
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Text(
                text = "Emergency unlock already used today.",
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
