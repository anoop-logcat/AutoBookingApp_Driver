package logcat.ayeautoapps.ayeautodriver2

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class MainMenu : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navView: BottomNavigationView = view.findViewById(R.id.bottomNav)
        val menu: Menu = navView.menu
        menu.findItem(R.id.navigationFragment).title = resourceLanguage?.getString(R.string.customer_view)
        menu.findItem(R.id.profileFragment).title = resourceLanguage?.getString(R.string.profile)
        val navController: NavController = activity?.let { Navigation.findNavController(it, R.id.fragment2) }!!
        NavigationUI.setupWithNavController(navView, navController)
    }
}