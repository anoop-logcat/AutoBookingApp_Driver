package logcat.ayeautoapps.ayeautodriver2.settings

import android.os.Bundle
import android.os.RecoverySystem
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.adapters.SettingAdapter
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class SettingMenu : Fragment() {

    private val arrayStrings= arrayOf(
        resourceLanguage?.getString(R.string.Privacy_Policy)!!,
        resourceLanguage?.getString(R.string.Help_Center)!!,resourceLanguage?.getString(R.string.About_Us)!!,
        resourceLanguage?.getString(R.string.Log_Out)!!
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingRecycler:RecyclerView=view.findViewById(R.id.settingList)
        settingRecycler.layoutManager=LinearLayoutManager(context,RecyclerView.VERTICAL,false)
        val adapter = SettingAdapter(requireContext(),arrayStrings)
        settingRecycler.adapter=adapter
    }
}