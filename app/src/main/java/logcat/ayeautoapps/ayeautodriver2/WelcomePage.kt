package logcat.ayeautoapps.ayeautodriver2

import `in`.aabhasjindal.otptextview.OtpTextView
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.messaging.FirebaseMessaging
import io.paperdb.Paper
import logcat.ayeautoapps.ayeautodriver2.adapters.SliderAdapter
import logcat.ayeautoapps.ayeautodriver2.models.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WelcomePage : Fragment(), AdapterView.OnItemClickListener {

    private lateinit var navController: NavController
    private lateinit var progressDialogInWelcome:ProgressDialog
    private lateinit var signUpButton:TextView
    private lateinit var signInButton:TextView

    private val sliderImages = listOf (R.drawable.tip1,R.drawable.tip2,R.drawable.tip3)
    private var signInDialog:BottomSheetDialog?=null
    private var user:FirebaseUser?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager:ViewPager2 = view.findViewById(R.id.sliderViewPager)
        val vDropdown:AutoCompleteTextView = view.findViewById(R.id.languageCategory)
        progressDialogInWelcome= ProgressDialog(requireActivity())
        signInButton = view.findViewById<Button>(R.id.signInSwitcher)
        signUpButton = view.findViewById<Button>(R.id.signUpSwitcher)
        signInDialog  =   BottomSheetDialog(requireActivity())
        viewPager.adapter = SliderAdapter(sliderImages)
        val lang = Array(2){""}
        lang[0] = "Malayalam"
        lang[1] = "English"
        val langAdapter=ArrayAdapter(requireActivity(),R.layout.drop_list_item,lang)
        vDropdown.setAdapter(langAdapter)
        vDropdown.onItemClickListener=this

        // button navigation's

        navController = Navigation.findNavController(view)
        signInButton.text = resourceLanguage?.getString(R.string.already_have_an_account)
        signInButton.setOnClickListener {
            pushSignInBottomSheet()
        }
        signUpButton.text = resourceLanguage?.getString(R.string.create_account)
        signUpButton.setOnClickListener {
            navController.navigate(R.id.action_welcomePage_to_signUpFragment)
        }
    }
    private fun pushSignInBottomSheet(){
        val   layoutBottomSheetView  = this.layoutInflater.inflate(R.layout.signinlayout, null)
        signInDialog?.setContentView(layoutBottomSheetView)
        signInDialog?.show()
        val title:TextView=layoutBottomSheetView.findViewById(R.id.tvTitle)
        val phoneSignInEdiText:TextInputEditText=layoutBottomSheetView.findViewById(R.id.signInEditText)
        val otpSignInEdiText:OtpTextView=layoutBottomSheetView.findViewById(R.id.otpEditTextForSignInVerify)
        val layoutConfirm:LinearLayout=layoutBottomSheetView.findViewById(R.id.signInConfirmLayout)
        val layoutVerify:LinearLayout=layoutBottomSheetView.findViewById(R.id.signInVerifyLayout)
        var verificationCodeToken:String?=null

        title.text= resourceLanguage?.getString(R.string.welcome_back)
        phoneSignInEdiText.hint=resourceLanguage?.getString(R.string.phone_number_2)
        layoutBottomSheetView.findViewById<Button>(R.id.signInConfirmButton).text=resourceLanguage?.getString(R.string.signin)
        layoutBottomSheetView.findViewById<Button>(R.id.signInConfirmButton_2).text=resourceLanguage?.getString(R.string.Verify_Account)

        layoutConfirm.visibility=View.VISIBLE
        layoutVerify.visibility=View.INVISIBLE
        layoutBottomSheetView.findViewById<Button>(R.id.signInConfirmButton).setOnClickListener {
            when {
                phoneSignInEdiText.text.isNullOrBlank() -> {
                    Toast.makeText(context, resourceLanguage?.getString(R.string.phone_field_is_empty),Toast.LENGTH_SHORT).show()
                }
                phoneSignInEdiText.text.toString().length!=10 -> {
                    Toast.makeText(context,resourceLanguage?.getString(R.string.phone_field_is_invalid),Toast.LENGTH_SHORT).show()
                }
                else -> {
                    signInDialog?.hide()
                    progressDialogInWelcome.setMessage(resourceLanguage!!.getString(R.string.generating_otp_code))
                    progressDialogInWelcome.setCancelable(false)
                    progressDialogInWelcome.show()
                    PhoneAuthProvider.getInstance().verifyPhoneNumber("+91${phoneSignInEdiText.text}", 60, TimeUnit.SECONDS, requireActivity(),
                        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            override fun onVerificationCompleted(credential: PhoneAuthCredential) {}
                            override fun onVerificationFailed(e: FirebaseException) {
                                progressDialogInWelcome.cancel()
                                signInDialog?.show()
                                if (e is FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(context, resourceLanguage?.getString(R.string.invalid_request), Toast.LENGTH_SHORT).show()
                                } else if (e is FirebaseTooManyRequestsException) {
                                    Toast.makeText(context, resourceLanguage?.getString(R.string.time_expired), Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                                progressDialogInWelcome.cancel()
                                signInDialog?.show()
                                verificationCodeToken=verificationId
                                layoutConfirm.visibility=View.INVISIBLE
                                layoutVerify.visibility=View.VISIBLE
                            }
                    })
                }
            }
        }
        layoutBottomSheetView.findViewById<Button>(R.id.signInConfirmButton_2).setOnClickListener {
            when {
                otpSignInEdiText.otp.isNullOrBlank() -> {
                    Toast.makeText(context, resourceLanguage?.getString(R.string.verify_empty),Toast.LENGTH_SHORT).show()
                }
                otpSignInEdiText.otp.toString().length==6 -> {
                    signInDialog?.hide()
                    progressDialogInWelcome.setMessage(resourceLanguage!!.getString(R.string.verifying_the_number))
                    progressDialogInWelcome.setCancelable(false)
                    progressDialogInWelcome.show()
                    val credential=PhoneAuthProvider.getCredential(verificationCodeToken!!,otpSignInEdiText.otp.toString())
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            user=task.result.user
                            CheckTheUser().execute(phoneSignInEdiText.text.toString())
                        }
                        else{
                            progressDialogInWelcome.cancel()
                            Toast.makeText(context, resourceLanguage!!.getString(R.string.failed_to_login),Toast.LENGTH_SHORT).show()
                            signInDialog?.cancel()
                        }
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class CheckTheUser : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/CheckDriverExist/${params[0]!!}"
            val request: Request = Request.Builder().url(url).build()
            val response= OkHttpClient().newCall(request).execute()
            return response.body()?.string().toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result!!.compareTo("USER EXIST")==0){
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task2 ->
                    if (task2.isSuccessful) {
                        val token = task2.result.toString()
                        FirebaseFunctions.getInstance().getHttpsCallable("UpdateDriver").call(mapOf("userID" to user!!.uid,"key" to "token","value" to token)).addOnCompleteListener { isChanged ->
                            when (isChanged.isSuccessful) {
                                true -> {
                                    progressDialogInWelcome.cancel()
                                    signInDialog?.cancel()
                                    navController.navigate(R.id.action_welcomePage_to_mainMenu)
                                }
                                else-> {
                                    progressDialogInWelcome.cancel()
                                    signInDialog?.cancel()
                                    Toast.makeText(context, resourceLanguage!!.getString(R.string.failed_to_login),Toast.LENGTH_SHORT).show()
                                    FirebaseAuth.getInstance().signOut()
                                }
                            }
                        }
                    }
                }
            }
            else{
                progressDialogInWelcome.cancel()
                signInDialog?.cancel()
                Toast.makeText(context, resourceLanguage?.getString(R.string.no_account),Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().currentUser?.delete()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Paper.init(requireContext())
        resourceLanguage = if(position==0){
            Paper.book().write("language","ml")
            val context: Context = LocaleHelper().setLocale(requireActivity(),Paper.book().read("language"))
            context.resources
        } else{
            Paper.book().write("language","en")
            val context: Context = LocaleHelper().setLocale(requireActivity(),Paper.book().read("language"))
            context.resources
        }
        signInButton.text = resourceLanguage?.getString(R.string.already_have_an_account)
        signUpButton.text = resourceLanguage?.getString(R.string.create_account)
    }
}