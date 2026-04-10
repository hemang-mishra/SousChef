package com.souschef.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper for Bluetooth Low Energy runtime permissions.
 *
 * Android 12+ (API 31+) requires BLUETOOTH_SCAN + BLUETOOTH_CONNECT instead of
 * the legacy BLUETOOTH permission. ACCESS_FINE_LOCATION is still required on
 * Android 11 and below for BLE scanning.
 */
object BlePermissionHelper {

    /** All permissions required for BLE scanning and connecting on the current API level. */
    val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    /**
     * Returns `true` when every required BLE permission is granted.
     */
    fun hasAllPermissions(context: Context): Boolean =
        requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }

    /**
     * Returns the subset of [requiredPermissions] that have NOT yet been granted.
     * Use this as the argument to `ActivityResultLauncher<Array<String>>.launch(...)`.
     */
    fun missingPermissions(context: Context): Array<String> =
        requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) !=
                    PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
}
