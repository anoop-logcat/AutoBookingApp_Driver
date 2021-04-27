package logcat.ayeautoapps.ayeautodriver2

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import io.paperdb.Paper
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleHelper().onAttach(newBase!!,"en"))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(this)
        if(Paper.book().read<String>("language")==null){
            Paper.book().write("language","en")
        }
        val context: Context = LocaleHelper().setLocale(this,Paper.book().read("language"))
        resourceLanguage = context.resources
        setContentView(R.layout.activity_main)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (notificationManager.getNotificationChannel(getString(R.string.notification_channel_default))==null || notificationManager.getNotificationChannel(getString(R.string.hire_notification_channel_default))==null)) {
            val audioAttributes = AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build()
            val name = "Auto Driver Notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(getString(R.string.notification_channel_default), name, importance)
            mChannel.enableVibration(true)
            mChannel.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"+packageName+"/raw/notification_sound"),audioAttributes)
            notificationManager.createNotificationChannel(mChannel)

            val name2 = "Hire Notification"
            val importance2 = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel2 = NotificationChannel(getString(R.string.hire_notification_channel_default), name2, importance2)
            notificationManager.createNotificationChannel(mChannel2)
        }
    }
}