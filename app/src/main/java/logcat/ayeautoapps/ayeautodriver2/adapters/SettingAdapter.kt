package logcat.ayeautoapps.ayeautodriver2.adapters

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import logcat.ayeautoapps.ayeautodriver2.MainActivity
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class SettingAdapter(private val context: Context, private val text1: Array<String>) : RecyclerView.Adapter<SettingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.settingmenulayout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(text1[position], context)
    }

    override fun getItemCount(): Int {
        return text1.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(text1: String, context: Context) {
            val startTextView: TextView = itemView.findViewById(R.id.txtTitle)
            val settingMenu: FrameLayout = itemView.findViewById(R.id.setting_each_menu)
            settingMenu.setOnClickListener {
                when {
                    startTextView.text.toString().compareTo(resourceLanguage?.getString(R.string.Privacy_Policy)!!) == 0 ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/10-Gsx0in_CWsp_fCUP0ia4K_YeVPmtMk/view?usp=drivesdk")))
                    startTextView.text.toString().compareTo(resourceLanguage?.getString(R.string.Help_Center)!!) == 0 ->
                        itemView.findNavController().navigate(R.id.action_settingMenu_to_helpCenter)
                    startTextView.text.toString().compareTo(resourceLanguage?.getString(R.string.About_Us)!!) == 0 ->
                        itemView.findNavController().navigate(R.id.action_settingMenu_to_aboutUs)
                    startTextView.text.toString().compareTo(resourceLanguage?.getString(R.string.Log_Out)!!) == 0 -> {
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(1)
                        FirebaseAuth.getInstance().signOut()
                        val i = Intent(context, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        i.putExtra("EXIT", true)
                        context.startActivity(i)
                    }
                }
            }
            startTextView.text = text1
        }
    }
}