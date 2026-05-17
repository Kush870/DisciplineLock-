package com.disciplinelock.app.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit
) {
    val usageSeconds by viewModel.dailyUsageSeconds.collectAsState()
    val limitMinutes by viewModel.dailyLimitMinutes.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val score by viewModel.disciplineScore.collectAsState()

    val usageMinutes = usageSeconds / 60
    val targetProgress = (usageMinutes.toFloat() / limitMinutes.toFloat()).coerceIn(0f, 1f)

    // Smooth animation for the progress ring
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "STREAK: $streak",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "SCORE: $score",
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "DISCIPLINE",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium,
            letterSpacing = 4.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        
        Spacer(modifier = Modifier.height(64.dp))

        // Focus Ring
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background track ring
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface,
                strokeWidth = 6.dp,
            )
            
            // Animated usage ring
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.onBackground,
                strokeWidth = 6.dp,
                strokeCap = StrokeCap.Round,
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${usageMinutes}m",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "of ${limitMinutes}m limit",
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        val isLimitReached = usageMinutes >= limitMinutes
        val message = if (isLimitReached) {
            "Time's up. Recover your focus."
        } else {
            "Stay intentional. The day counts."
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(if (isLimitReached) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.uppercase(),
                color = if (isLimitReached) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
