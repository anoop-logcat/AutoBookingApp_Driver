package logcat.ayeautoapps.ayeautodriver2

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import logcat.ayeautoapps.ayeautodriver2.models.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SignUpFragment : Fragment() {

    private lateinit var standSelection: TextView
    private lateinit var openTime:TextView
    private lateinit var closeTime:TextView
    private lateinit var username: EditText
    private lateinit var phone:TextView
    private lateinit var age:EditText
    private lateinit var autoNumber:EditText
    private lateinit var progressInSignUp:ProgressDialog

    private var navController: NavController?=null
    private val argInSignUpFragment:SignUpFragmentArgs by navArgs()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        username = view.findViewById(R.id.signUpUserName)
        phone = view.findViewById(R.id.signUpPhone)
        age = view.findViewById(R.id.signUpAge)
        autoNumber = view.findViewById(R.id.signUpAutoNum)
        standSelection = view.findViewById(R.id.signUpAutoStandSelection)
        openTime = view.findViewById(R.id.signUpOpenTime)
        closeTime = view.findViewById(R.id.signUpCloseTime)
        progressInSignUp = ProgressDialog(requireActivity())
        view.findViewById<TextView>(R.id.title_signUp).text = resourceLanguage?.getString(R.string.create_account)
        view.findViewById<TextView>(R.id.workingTitle).text = resourceLanguage?.getString(R.string.working_time)
        openTime.text=resourceLanguage?.getString(R.string.start)
        closeTime.text=resourceLanguage?.getString(R.string.end)
        username.hint=resourceLanguage?.getString(R.string.user_name)
        age.hint=resourceLanguage?.getString(R.string.your_age)
        autoNumber.hint=resourceLanguage?.getString(R.string.auto_number_kl_00_0000)
        if(argInSignUpFragment.standDetails.compareTo("stand details")==0){standSelection.hint = resourceLanguage?.getString(R.string.stand_details)}else{standSelection.text = argInSignUpFragment.standDetails}
        if(argInSignUpFragment.phoneNumber.compareTo("phone number")==0){phone.hint = resourceLanguage?.getString(R.string.phone_number)}else{phone.text = argInSignUpFragment.phoneNumber}

        // setting the time in text fields

        openTime.setOnClickListener {
            timeSelector(openTime)
        }
        closeTime.setOnClickListener {
            timeSelector(closeTime)
        }

        // back button setting

        view.findViewById<ImageView>(R.id.signUpBackButton).setOnClickListener {
            navController!!.navigate(R.id.action_signUpFragment_to_welcomePage)
        }

        // sign up submit button

        view.findViewById<RelativeLayout>(R.id.SignUpSubmitButton).setOnClickListener {
            if(username.text.isNullOrBlank()||phone.text.isNullOrBlank()||age.text.isNullOrBlank()||autoNumber.text.isNullOrBlank()||openTime.text.isNullOrBlank()||closeTime.text.isNullOrBlank()||standSelection.text.isNullOrBlank()){
                Toast.makeText(context, resourceLanguage?.getString(R.string.Empty_fields), Toast.LENGTH_SHORT).show()
            }
            else{
               CheckTheUser().execute(phone.text.toString())
            }
        }

        // navigate to stand selection page

        standSelection.setOnClickListener {
            navController!!.navigate(R.id.action_signUpFragment_to_standSelectionFragment)
        }

        phone.setOnClickListener {
            if(phone.text.isNullOrBlank()){
                Toast.makeText(context, resourceLanguage?.getString(R.string.First_Select_the_Auto_Stand), Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class CheckTheUser : AsyncTask<String, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            progressInSignUp.setMessage("Verifying the account")
            progressInSignUp.setCancelable(false)
            progressInSignUp.show()
        }
        override fun doInBackground(vararg params: String?): String {
            val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/CheckDriverExist/${params[0]!!}"
            val request: Request = Request.Builder().url(url).build()
            val response= OkHttpClient().newCall(request).execute()
            return response.body()?.string().toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result!!.compareTo("USER EXIST")==0){
                progressInSignUp.cancel()
                Toast.makeText(context, resourceLanguage!!.getString(R.string.existing_account_use_login),Toast.LENGTH_SHORT).show()
            }
            else{
                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91${phone.text}", 60, TimeUnit.SECONDS, requireActivity(),
                        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                            override fun onVerificationFailed(e: FirebaseException) {
                                if (e is FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(context, resourceLanguage?.getString(R.string.invalid_request), Toast.LENGTH_SHORT).show()
                                } else if (e is FirebaseTooManyRequestsException) {
                                    Toast.makeText(context, resourceLanguage?.getString(R.string.time_expired), Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                progressInSignUp.cancel()
                                code = verificationId
                                userData = mutableMapOf(
                                        "username" to username.text.toString(),
                                        "phone" to phone.text.toString(),
                                        "age" to age.text.toString(),
                                        "isHiring" to false,
                                        "currentLatitude" to 1.0,
                                        "currentLongitude" to 1.0,
                                        "imageUrl" to "no image",
                                        "testMode" to argInSignUpFragment.standMode,
                                        "autoNumber" to autoNumber.text.toString(),
                                        "workingTime" to openTime.text.toString()+" - "+closeTime.text.toString(),
                                        "standName" to argInSignUpFragment.standDetails.split(",").first(),
                                        "standRep" to argInSignUpFragment.standNominee,
                                        "standLandMark" to argInSignUpFragment.standDetails.split(",").last(),
                                )
                                navController!!.navigate(R.id.action_signUpFragment_to_verificationFragment)
                            }
                })
            }
        }
    }

    private fun timeSelector(time: TextView){
        val mDialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.timelayout, null)
        val mBuilder = MaterialAlertDialogBuilder(requireActivity()).setView(mDialogView)
        val timePicker:TimePicker=mDialogView.findViewById(R.id.timeView)
        val timeOkButton:Button=mDialogView.findViewById(R.id.ok_time_picker)
        val cancelOkButton:Button=mDialogView.findViewById(R.id.cancel_time_picker)
        val dialog=mBuilder.show()
        timeOkButton.text= resourceLanguage?.getString(R.string.accept)
        timeOkButton.setOnClickListener {
            dialog.cancel()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                when {
                    timePicker.hour > 12 -> {
                        if((timePicker.hour-12) in 10..12){
                            time.text = "${timePicker.hour-12}:${if(timePicker.minute.toString().length==1){"0${timePicker.minute}"}else{"${timePicker.minute}"}} PM"
                        }else{
                            time.text = "0${timePicker.hour-12}:${if(timePicker.minute.toString().length==1){"0${timePicker.minute}"}else{"${timePicker.minute}"}} PM"
                        }
                    }
                    timePicker.hour in 10..12 -> {
                        time.text = "${timePicker.hour}:${if(timePicker.minute.toString().length==1){"0${timePicker.minute}"}else{"${timePicker.minute}"}} AM"
                    }
                    timePicker.hour==0 -> {
                        time.text = "12:${if(timePicker.minute.toString().length==1){"0${timePicker.minute}"}else{"${timePicker.minute}"}} AM"
                    }
                    else -> {
                        time.text = "0${timePicker.hour}:${if(timePicker.minute.toString().length==1){"0${timePicker.minute}"}else{"${timePicker.minute}"}} AM"
                    }
                }
            }
        }
        cancelOkButton.text= resourceLanguage?.getString(R.string.cancel)
        cancelOkButton.setOnClickListener {
            dialog.cancel()
        }
    }
}