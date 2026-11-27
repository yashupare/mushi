package com.mushi.app.update

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.IntentCompat
import androidx.core.content.edit
import com.mushi.app.R

private const val TAG = "AppUpdate"
private const val PREFS = "app_update"
private const val UPDATING_ITSELF = "updating_itself"
private const val CHANNEL = "app_updates"
private const val NOTIFICATION_ID = 7001

internal fun markUpdatingItself(context: Context) {
    // Written before the install, which ends this process
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit(commit = true) {
        putBoolean(UPDATING_ITSELF, true)
    }
}

/** Hears how an install went. A successful one usually ends the app before this runs. */
class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                    ?: return
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Android blocks opening a screen from the background, so ask by notification there
                if (appIsOnScreen()) context.startActivity(confirm)
                else notify(context, R.string.update_confirm_title, R.string.update_tap_to_install, confirm)
            }

            PackageInstaller.STATUS_SUCCESS -> Unit
            else -> {
                Log.w(TAG, "Install failed with status $status: " +
                    intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE))
                AndroidAppInstaller.installFailures.tryEmit(Unit)
            }
        }
    }

    private fun appIsOnScreen(): Boolean {
        val state = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(state)
        return state.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}

/** Updating closes the app and Android won't reopen it, so say that it can be opened again. */
class AppReplacedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Not for updates installed some other way
        if (!prefs.getBoolean(UPDATING_ITSELF, false)) return
        prefs.edit { remove(UPDATING_ITSELF) }
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        notify(context, R.string.update_done_title, R.string.update_tap_to_open, open)
    }
}

private fun notify(context: Context, title: Int, text: Int, onTap: Intent) {
    val notifications = NotificationManagerCompat.from(context)
    if (!notifications.areNotificationsEnabled()) return
    notifications.createNotificationChannel(
        NotificationChannel(
            CHANNEL, context.getString(R.string.update_channel_name), NotificationManager.IMPORTANCE_DEFAULT))
    val notification = NotificationCompat.Builder(context, CHANNEL)
        .setSmallIcon(R.drawable.ic_stat_mushi)
        .setContentTitle(context.getString(title))
        .setContentText(context.getString(text))
        .setAutoCancel(true)
        .setContentIntent(
            PendingIntent.getActivity(
                context, 0, onTap, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        .build()
    try {
        notifications.notify(NOTIFICATION_ID, notification)
    } catch (e: SecurityException) {
        Log.w(TAG, "Notifications aren't allowed", e)
    }
}
