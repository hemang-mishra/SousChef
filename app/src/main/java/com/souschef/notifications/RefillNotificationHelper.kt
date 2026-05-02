package com.souschef.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.souschef.MainActivity
import com.souschef.model.device.Compartment
import com.souschef.permissions.NotificationPermissionHelper

/**
 * Helper for posting local refill alerts when a dispenser compartment runs low.
 *
 * Local notifications only — no FCM/push.
 */
object RefillNotificationHelper {

    private const val CHANNEL_ID = "souschef_refill"
    private const val CHANNEL_NAME = "Refill alerts"
    private const val CHANNEL_DESC = "Reminders to refill your dispenser compartments"

    /** Per-compartment notification id offset; safe range below 100k. */
    private const val NOTIFICATION_ID_BASE = 1000

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
        )
    }

    fun notifyLowStock(context: Context, compartment: Compartment) {
        if (!NotificationPermissionHelper.hasPermission(context)) return
        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            compartment.compartmentId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val name = compartment.ingredientName ?: "Compartment ${compartment.compartmentId}"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$name running low")
            .setContentText("Only ${"%.1f".format(compartment.remainingTsp)} tsp left — time for a refill.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Compartment #${compartment.compartmentId} ($name) has " +
                            "${"%.1f".format(compartment.remainingTsp)} tsp remaining " +
                            "of ${"%.1f".format(compartment.totalCapacityTsp)} tsp. Refill it from the Dispenser tab."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_BASE + compartment.compartmentId, notification)
        } catch (_: SecurityException) {
            // Permission revoked at runtime — silently ignore.
        }
    }
}
