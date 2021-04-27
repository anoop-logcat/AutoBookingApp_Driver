package logcat.ayeautoapps.ayeautodriver2.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class HelpCenter : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_help__center, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<CardView>(R.id.call_anoop).setOnClickListener {
            checkPermissionAndCall("+919207304407")
        }
        view.findViewById<CardView>(R.id.call_tony).setOnClickListener {
            checkPermissionAndCall("+919526552062")
        }
        view.findViewById<CardView>(R.id.call_snehal).setOnClickListener {
            checkPermissionAndCall("+919567496950")
        }
        view.findViewById<TextView>(R.id.send_feedback_button).setOnClickListener {
            sendFeedBack()
        }
    }

    private fun sendFeedBack(){
        val email = Intent(Intent.ACTION_SENDTO)
        email.data = Uri.parse("mailto:logcatsolutions@gmail.com")
        startActivity(email)
    }

    private fun checkPermissionAndCall(number:String) {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 47)
            }
            else{
                Toast.makeText(context, resourceLanguage?.getString(R.string.Permission_denied), Toast.LENGTH_SHORT).show()
            }
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            startActivity(intent)
        }
    }
}