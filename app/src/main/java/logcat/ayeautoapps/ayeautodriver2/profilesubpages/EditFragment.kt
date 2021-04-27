@file:Suppress("DEPRECATION")

package logcat.ayeautoapps.ayeautodriver2.profilesubpages

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.models.driverObject
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage


class EditFragment : Fragment() {

    private val dialogBoxImages= arrayOf(
            R.drawable.dialogbox_userpic,
            R.drawable.dialogbox_phone,
            R.drawable.dialogbox_age,
            R.drawable.autologo,
            R.drawable.dialogbox_time
    )
    private var viewToFunctions:View?=null

    private lateinit var usernameInEditWindow:TextView
    private lateinit var ageInEditWindow:TextView
    private lateinit var autoNumberInEditWindow:TextView
    private lateinit var timeInEditWindow:TextView

    private val user= FirebaseAuth.getInstance().currentUser

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewToFunctions=view
        usernameInEditWindow=view.findViewById(R.id.editUserNameDisplay)
        ageInEditWindow=view.findViewById(R.id.editAgeDisplay)
        autoNumberInEditWindow=view.findViewById(R.id.editAutoNumberDisplay)
        timeInEditWindow=view.findViewById(R.id.editTimeDisplay)

        view.findViewById<TextView>(R.id.userTextView).text= resourceLanguage?.getString(R.string.user_name)
        view.findViewById<TextView>(R.id.ageTextView).text= resourceLanguage?.getString(R.string.your_age)
        view.findViewById<TextView>(R.id.autoNumberTextView).text= resourceLanguage?.getString(R.string.auto_number_kl_00_0000)
        view.findViewById<TextView>(R.id.timeTextView).text= resourceLanguage?.getString(R.string.working_time)

        view.findViewById<AVLoadingIndicatorView>(R.id.progress_1).hide()
        view.findViewById<AVLoadingIndicatorView>(R.id.progress_3).hide()
        view.findViewById<AVLoadingIndicatorView>(R.id.progress_4).hide()
        view.findViewById<AVLoadingIndicatorView>(R.id.progress_5).hide()

        setData()

        view.findViewById<ImageView>(R.id.usernameEdit).setOnClickListener {
            editingDialogBox(dialogBoxImages[0], "${resourceLanguage?.getString(R.string.user_name)} ", R.id.editUserNameDisplay, InputType.TYPE_CLASS_TEXT,R.id.progress_1, false)
        }
        view.findViewById<ImageView>(R.id.ageEdit).setOnClickListener {
            editingDialogBox(dialogBoxImages[2], "${resourceLanguage?.getString(R.string.your_age)} ", R.id.editAgeDisplay, InputType.TYPE_CLASS_NUMBER,R.id.progress_3, false)
        }
        view.findViewById<ImageView>(R.id.autoNumberEdit).setOnClickListener {
            editingDialogBox(dialogBoxImages[3], "${resourceLanguage?.getString(R.string.auto_number_kl_00_0000)} ", R.id.editAutoNumberDisplay, InputType.TYPE_CLASS_TEXT,R.id.progress_4,false)
        }
        view.findViewById<ImageView>(R.id.workingTimeEdit).setOnClickListener {
            editingDialogBox(dialogBoxImages[4], "${resourceLanguage?.getString(R.string.working_time)} ", R.id.editTimeDisplay, InputType.TYPE_CLASS_TEXT,R.id.progress_5, true)
        }
    }

    private fun editingDialogBox(imageID: Int, title: String, textFieldID: Int, inputType: Int, loaderID:Int, isTime: Boolean){
        val mDialogView = LayoutInflater.from(requireActivity()).inflate(R.layout.editdialoglayout, null)
        val mBuilder = MaterialAlertDialogBuilder(requireActivity()).setView(mDialogView)
        mBuilder.background = resources.getDrawable(R.drawable.dialogbackgroundstyle)
        val preViewImage:ImageView=mDialogView.findViewById(R.id.dialogImagePreview)
        val titleTextView:TextView=mDialogView.findViewById(R.id.titleTextView)

        val inputLayout:TextInputLayout=mDialogView.findViewById(R.id.dialogInputLayout)
        val timeLayout:LinearLayout=mDialogView.findViewById(R.id.dialogBoxTimeLayout)

        val dialogEditText: TextInputEditText =mDialogView.findViewById(R.id.dialogEditText)
        val startTime:MaterialTextView=mDialogView.findViewById(R.id.editTimeStart)
        val endTime:MaterialTextView=mDialogView.findViewById(R.id.editTimeEnd)
        startTime.hint= resourceLanguage?.getString(R.string.start)
        endTime.hint= resourceLanguage?.getString(R.string.end)

        dialogEditText.hint=title
        when(isTime){
            true -> {
                inputLayout.visibility = View.INVISIBLE
                timeLayout.visibility = View.VISIBLE
                startTime.setOnClickListener {
                    timeSelector(startTime)
                }
                endTime.setOnClickListener {
                    timeSelector(endTime)
                }
            }
            false -> {
                inputLayout.visibility = View.VISIBLE
                dialogEditText.inputType = inputType
                timeLayout.visibility = View.INVISIBLE
            }
        }
        titleTextView.text=title
        preViewImage.setImageResource(imageID)
        val dialog=mBuilder.show()
        mDialogView.findViewById<Button>(R.id.dialogOk).text= resourceLanguage?.getString(R.string.accept)
        mDialogView.findViewById<Button>(R.id.dialogOk).setOnClickListener {
            if(inputLayout.isVisible){
                if(!dialogEditText.text.isNullOrEmpty()){
                    viewToFunctions?.findViewById<AVLoadingIndicatorView>(loaderID)?.show()
                    when(textFieldID){
                        usernameInEditWindow.id ->{
                            usernameInEditWindow.text=""
                            changeDriverData("username",dialogEditText.text.toString(),usernameInEditWindow,loaderID)
                        }
                        ageInEditWindow.id -> {
                            ageInEditWindow.text=""
                            changeDriverData("age",dialogEditText.text.toString(),ageInEditWindow,loaderID)
                        }
                        autoNumberInEditWindow.id -> {
                            autoNumberInEditWindow.text=""
                            changeDriverData("autoNumber",dialogEditText.text.toString(),autoNumberInEditWindow,loaderID)
                        }
                    }
                    dialog.cancel()
                }
                else{
                    Toast.makeText(context, resourceLanguage?.getString(R.string.Empty_fields),Toast.LENGTH_SHORT).show()
                }
            }
            else{
                if(!startTime.text.isNullOrEmpty()&&!endTime.text.isNullOrEmpty()){
                    viewToFunctions?.findViewById<AVLoadingIndicatorView>(loaderID)?.show()
                    timeInEditWindow.text=""
                    changeDriverData("workingTime", "${startTime.text} - ${endTime.text}",timeInEditWindow,loaderID)
                    dialog.cancel()
                }
                else{
                    Toast.makeText(context, resourceLanguage?.getString(R.string.Empty_fields),Toast.LENGTH_SHORT).show()
                }
            }
        }
        mDialogView.findViewById<Button>(R.id.dialogCancel).text= resourceLanguage?.getString(R.string.cancel)
        mDialogView.findViewById<Button>(R.id.dialogCancel).setOnClickListener {
            dialog.cancel()
        }
    }

    private fun timeSelector(time: MaterialTextView){
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

    private fun changeDriverData(key:String,value:String,textView:TextView,loaderID:Int){
        FirebaseFunctions.getInstance().getHttpsCallable("UpdateDriver").call(mapOf("userID" to user!!.uid,"key" to key,"value" to value)).addOnCompleteListener { isChanged ->
            when (!isChanged.isSuccessful) {true->Toast.makeText(context, "Failed to change", Toast.LENGTH_SHORT).show()
                                            else->{
                                                when (textView.id) {
                                                    R.id.editUserNameDisplay -> {
                                                        driverObject?.username=value
                                                        textView.text=value
                                                        val intent = Intent()
                                                        intent.putExtra("USERNAME_CHANGE_IN_PROFILE", value)
                                                        intent.action = "com.my.app.onProfileNameReceived"
                                                        requireActivity().sendBroadcast(intent)
                                                    }
                                                    R.id.editAgeDisplay -> {
                                                        driverObject?.age = value
                                                        textView.text="$value Years"
                                                    }
                                                    R.id.editAutoNumberDisplay -> {
                                                        driverObject?.autoNumber = value
                                                        textView.text=value
                                                    }
                                                    R.id.editTimeDisplay -> {
                                                        driverObject?.workingTime = value
                                                        textView.text=value
                                                    }
                                                }
                                                viewToFunctions?.findViewById<AVLoadingIndicatorView>(loaderID)?.hide()
                                            }
            }
        }
    }

    private fun setData(){
        usernameInEditWindow.text= driverObject?.username
        ageInEditWindow.text= "${driverObject?.age}  Years"
        autoNumberInEditWindow.text= driverObject?.autoNumber
        timeInEditWindow.text= driverObject?.workingTime
    }

}