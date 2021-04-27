package logcat.ayeautoapps.ayeautodriver2

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessageServices : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            when {
                it.title.toString().compareTo("New Customer")==0 -> {
                    val intent = Intent()
                    intent.putExtra("CUSTOMER_DATA", remoteMessage.data.toString())
                    intent.action = "com.my.app.onMessageReceived"
                    sendBroadcast(intent)
                }
                it.title.toString().compareTo("Stand Mode Changed")==0 -> {
                    val intent = Intent()
                    intent.putExtra("STAND_MODE_CHANGE_IN_PROFILE", remoteMessage.data.toString())
                    intent.action = "com.my.app.onProfileNameReceived"
                    sendBroadcast(intent)
                }
                it.title.toString().compareTo("Stand Nominee Changed")==0 -> {
                    val intent = Intent()
                    intent.putExtra("STAND_NOMINEE_CHANGE_IN_PROFILE", remoteMessage.data.toString())
                    intent.action = "com.my.app.onProfileNameReceived"
                    sendBroadcast(intent)
                }
            }
            sendNotification(it.title.toString(), it.body.toString())
        }
    }

    override fun onNewToken(token: String) { }

    private fun sendNotification(messageTitle: String, messageBody: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = NotificationCompat.Builder(this, getString(R.string.notification_channel_default))
                .setSmallIcon(R.drawable.autologo)
                .setColor(resources.getColor(R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.driverr_icon))
                .setContentTitle(messageTitle)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/raw/notification_sound"))
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}