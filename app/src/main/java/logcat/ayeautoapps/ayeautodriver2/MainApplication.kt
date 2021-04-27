package logcat.ayeautoapps.ayeautodriver2

import android.app.Application
import android.content.Context

class MainApplication: Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper().onAttach(base!!,"en"))
    }
}