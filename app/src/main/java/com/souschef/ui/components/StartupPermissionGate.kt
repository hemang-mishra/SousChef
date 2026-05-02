package com.souschef.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.souschef.permissions.BlePermissionHelper
import com.souschef.permissions.NotificationPermissionHelper
import com.souschef.preferences.AppPreferences
import com.souschef.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * One-time bottom sheet shown after first login that asks the user to grant
 * BLE + notification permissions. After the user dismisses or responds, the
 * gate flag is set and never shown again on this device.
 *
 * The wrapped [content] is rendered behind the sheet at all times.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartupPermissionGate(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val prefs: AppPreferences = koinInject()
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    // On first composition decide whether to show the gate
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        val asked = prefs.startupPermissionsRequested.get()
        val needsBle = !BlePermissionHelper.hasAllPermissions(context)
        val needsNotif = !NotificationPermissionHelper.hasPermission(context)
        if (!asked && (needsBle || needsNotif)) {
            showSheet = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        scope.launch { prefs.startupPermissionsRequested.set(true) }
        showSheet = false
    }

    content()

    if (showSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { prefs.startupPermissionsRequested.set(true) }
                showSheet = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PermissionGateContent(
                onGrant = {
                    val perms = (BlePermissionHelper.requiredPermissions +
                            NotificationPermissionHelper.requiredPermissions).distinct().toTypedArray()
                    if (perms.isNotEmpty()) {
                        launcher.launch(perms)
                    } else {
                        scope.launch { prefs.startupPermissionsRequested.set(true) }
                        showSheet = false
                    }
                },
                onSkip = {
                    scope.launch { prefs.startupPermissionsRequested.set(true) }
                    showSheet = false
                }
            )
        }
    }
}

@Composable
private fun PermissionGateContent(
    onGrant: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to SousChef",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AppColors.textPrimary()
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Grant a couple of permissions so the app can pair with your dispenser and remind you to refill spices.",
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.textSecondary(),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        PermissionRow(
            icon = Icons.Default.Bluetooth,
            title = "Bluetooth",
            description = "Connect to your SousChef Dispenser to auto-dispense spices."
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            description = "Get a heads-up when a compartment is running low so you can refill it."
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onGrant,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.gold())
        ) {
            Text("Continue", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = onSkip, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)) {
            Text("Maybe later", color = AppColors.textSecondary())
        }
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(AppColors.gold().copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = AppColors.gold(), modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.textPrimary()
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.textSecondary()
            )
        }
    }
}
