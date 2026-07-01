package com.poke.dex.core.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.poke.dex.R
import com.poke.dex.presentation.main.MainActivity

class MyFirebaseNotificationService : FirebaseMessagingService() {

    private val tag = "FCMService"
    private val channel_id = "pokedex_notifications"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "Refreshed FCM device token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "PokeDex Updated!"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Check out the latest updates."

        if (isAppInForeground()) {
            sendLocalBroadcast(title, body)
        } else {
            showNotification(title, body)
        }
    }

    private fun isAppInForeground(): Boolean {
        val currentState = ProcessLifecycleOwner.get().lifecycle.currentState
        return currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private fun sendLocalBroadcast(title: String, body: String) {
        val intent = Intent(ACTION_FOREGROUND_NOTIFICATION).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channel_id,
                "PokéDex Announcements",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channels alerts for new Pokémon releases or events"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channel_id)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    companion object {
        const val ACTION_FOREGROUND_NOTIFICATION = "com.poke.dex.FOREGROUND_NOTIFICATION"
        const val EXTRA_TITLE = "notification_title"
        const val EXTRA_BODY = "notification_body"
    }
}
