package logcat.ayeautoapps.ayeautodriver2.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import logcat.ayeautoapps.ayeautodriver2.profilesubpages.EditFragment
import logcat.ayeautoapps.ayeautodriver2.profilesubpages.HistoryFragment
import logcat.ayeautoapps.ayeautodriver2.ProfileFragment

class ProfilePageAdapter(fragmentActivity: ProfileFragment) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 2
    }
    override fun createFragment(position: Int): Fragment {
        return when(position){
            0-> EditFragment()
            else-> HistoryFragment()
        }
    }
}