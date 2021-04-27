package logcat.ayeautoapps.ayeautodriver2

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage

class SplashScreen : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController:NavController= Navigation.findNavController(view)
        Handler().postDelayed({
            val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
            if(currentUser!=null){
                navController.navigate(R.id.action_splashScreen_to_mainMenu)
            }
            else{
                navController.navigate(R.id.action_splashScreen_to_welcomePage)
            }
        }, 2000)
    }
}