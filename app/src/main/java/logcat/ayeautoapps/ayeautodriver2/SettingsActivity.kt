package logcat.ayeautoapps.ayeautodriver2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val toolBar= supportActionBar
        toolBar?.title= resourceLanguage?.getString(R.string.settings)
        toolBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}