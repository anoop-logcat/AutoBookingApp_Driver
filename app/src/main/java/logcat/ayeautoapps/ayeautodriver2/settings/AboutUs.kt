package logcat.ayeautoapps.ayeautodriver2.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import logcat.ayeautoapps.ayeautodriver2.R


class AboutUs : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about_us, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ImageView>(R.id.facebook_link_button).setOnClickListener {
            try {
                requireContext().packageManager.getPackageInfo("com.facebook.katana", 0)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/103749441608531")))
            } catch (e: java.lang.Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/LogcatSolutions")))
            }
        }
        view.findViewById<ImageView>(R.id.instagram_link_button).setOnClickListener {
            try {
                val intentInstagram = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/logcat__technologies?r=nametag"))
                intentInstagram.setPackage("com.instagram.android")
                startActivity(intentInstagram)
            }
            catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/logcat__technologies?r=nametag")))
            }
        }
        view.findViewById<ImageView>(R.id.website_link_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.logcatsolutions.com/")))
        }
    }

}