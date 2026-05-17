package com.disciplinelock.app.ui.permissions

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@Composable
fun PermissionsScreen(onPermissionsGranted: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageAccess by remember { mutableStateOf(checkUsageAccess(context)) }
    var hasOverlayAccess by remember { mutableStateOf(checkOverlayAccess(context)) }
    var hasAccessibilityAccess by remember { mutableStateOf(checkAccessibilityAccess(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = checkUsageAccess(context)
                hasOverlayAccess = checkOverlayAccess(context)
                hasAccessibilityAccess = checkAccessibilityAccess(context)
                if (hasUsageAccess && hasOverlayAccess && hasAccessibilityAccess) {
                    onPermissionsGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(hasUsageAccess, hasOverlayAccess, hasAccessibilityAccess) {
        if (hasUsageAccess && hasOverlayAccess && hasAccessibilityAccess) {
            onPermissionsGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Commitment Requires Access",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "To help you keep promises to yourself, this system needs strict permissions.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        PermissionItem(
            title = "Usage Access",
            description = "Required to track Instagram usage.",
            isGranted = hasUsageAccess,
            onClick = {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PermissionItem(
            title = "Display Over Other Apps",
            description = "Required to block Instagram when limit is reached.",
            isGranted = hasOverlayAccess,
            onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PermissionItem(
            title = "Accessibility Service",
            description = "Required to accurately track when Instagram opens and closes.",
            isGranted = hasAccessibilityAccess,
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isGranted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            enabled = !isGranted,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledContentColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(if (isGranted) "Granted" else "Grant Permission")
        }
    }
}

private fun checkUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.checkOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

private fun checkOverlayAccess(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

private fun checkAccessibilityAccess(context: Context): Boolean {
    var accessibilityEnabled = 0
    try {
        accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
        )
    } catch (e: Settings.SettingNotFoundException) {
        // Assume not enabled if exception occurs
    }
    
    if (accessibilityEnabled == 1) {
        val services = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        if (services != null) {
            val componentName = ComponentName(context, com.disciplinelock.app.service.InstagramTrackingService::class.java)
            val flattenedComponent = componentName.flattenToString()
            return services.contains(flattenedComponent)
        }
    }
    return false
}
