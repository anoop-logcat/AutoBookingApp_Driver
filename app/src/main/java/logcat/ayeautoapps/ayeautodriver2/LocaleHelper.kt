package logcat.ayeautoapps.ayeautodriver2

import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

class LocaleHelper{
    private val SELECTED_LANGUAGE:String="Locale.Helper.Selected.Language"

    fun onAttach(context:Context):Context{
        val lang:String = getPersistedData(context,Locale.getDefault().language)
        return setLocale(context,lang)
    }

    fun onAttach(context:Context,defaultLanguage: String):Context{
        val lang:String = getPersistedData(context,defaultLanguage)
        return setLocale(context,lang)
    }

    fun setLocale(context: Context, lang: String): Context {
        persist(context,lang)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
            return updateResources(context,lang)
        }
        return updateResourcesLegacy(context,lang)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, lang: String): Context {
        val locale:Locale = Locale(lang)
        Locale.setDefault(locale)

        val config:Configuration = context.resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    @SuppressWarnings("deprecation")
    private fun updateResourcesLegacy(context: Context, lang: String): Context {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val resource: Resources = context.resources
        val config:Configuration = context.resources.configuration
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            config.setLayoutDirection(locale)
        }
        resource.updateConfiguration(config, resource.displayMetrics)
        return context
    }

    private fun persist(context: Context, lang: String) {
        val pref2:SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor:SharedPreferences.Editor = pref2.edit()
        editor.putString(SELECTED_LANGUAGE,lang)
        editor.apply()
    }

    private fun getPersistedData(context: Context, language: String): String {
        val pref:SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getString(SELECTED_LANGUAGE,language)!!
    }
}