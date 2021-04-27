package logcat.ayeautoapps.ayeautodriver2

import `in`.aabhasjindal.otptextview.OtpTextView
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import logcat.ayeautoapps.ayeautodriver2.models.code
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage
import logcat.ayeautoapps.ayeautodriver2.models.userData

class VerificationFragment : Fragment() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController: NavController = Navigation.findNavController(view)
        val closeButton=view.findViewById(R.id.closeForVerify) as ImageView
        val otpEditText=view.findViewById(R.id.otpEditTextForVerify) as OtpTextView
        val verifyButton=view.findViewById(R.id.verifyButton) as Button
        view.findViewById<AppCompatTextView>(R.id.title_verification_page).text= resourceLanguage?.getString(R.string.Verify_Your_Phone_Number)
        view.findViewById<AppCompatTextView>(R.id.title_2_verification_page).text= resourceLanguage?.getString(R.string.Enter_your_OTP_code_here)
        view.findViewById<TextView>(R.id.terms_and_condition_text).text= resourceLanguage?.getString(R.string.terms_and_conditions)
        verifyButton.text= resourceLanguage?.getString(R.string.Verify_Account)

        closeButton.setOnClickListener {
            navController.navigate(R.id.action_verificationFragment_to_signUpFragment)
        }

        verifyButton.setOnClickListener {
            if(otpEditText.otp?.length==6){
                val progressDialog =  ProgressDialog(requireActivity())
                progressDialog.setMessage(resourceLanguage!!.getString(R.string.verifying_the_number))
                progressDialog.setCancelable(false)
                progressDialog.show()
                val credential=PhoneAuthProvider.getCredential(code,otpEditText.otp!!)
                auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                userData["token"] = task2.result.toString()
                                FirebaseFunctions.getInstance().getHttpsCallable("SaveDriver").call(userData).addOnCompleteListener { task3->
                                    if (task3.isSuccessful) {
                                        progressDialog.cancel()
                                        navController.navigate(R.id.action_verificationFragment_to_mainMenu)
                                    }
                                    else {
                                        progressDialog.cancel()
                                        task.result?.user?.delete()
                                        Toast.makeText(context, resourceLanguage?.getString(R.string.Uploading_data_failed), Toast.LENGTH_SHORT).show()
                                        navController.navigate(R.id.action_verificationFragment_to_signUpFragment)
                                    }
                                }
                            }
                        }
                    }
                    else {
                        progressDialog.cancel()
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(context, resourceLanguage?.getString(R.string.Invalid_Code) , Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}