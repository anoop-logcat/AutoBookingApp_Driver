package logcat.ayeautoapps.ayeautodriver2

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import logcat.ayeautoapps.ayeautodriver2.models.hireChecker


class HireBroadCastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        if(intent.getStringExtra("HIRE_DATA")!=null){
            val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(1)
            hireChecker.isChecked=false
        }
    }
}