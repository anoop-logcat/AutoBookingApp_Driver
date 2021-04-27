
package logcat.ayeautoapps.ayeautodriver2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView
import de.hdodenhof.circleimageview.CircleImageView
import logcat.ayeautoapps.ayeautodriver2.adapters.ProfilePageAdapter
import logcat.ayeautoapps.ayeautodriver2.models.*
import okhttp3.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class ProfileFragment : Fragment() {

    private val receiver = MyProfileBroadCastReceiver()
    private var profileView:View?=null

    private val storageReference: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://auto-pickup-apps.appspot.com")
    private val user= FirebaseAuth.getInstance().currentUser


    private lateinit var profilePic: CircleImageView
    private lateinit var changePic:ImageView
    private lateinit var profileName:TextView
    private lateinit var profileNumber:TextView
    private lateinit var profileStand:TextView
    private lateinit var profileStandMode:TextView
    private lateinit var profileStandNominee:TextView
    private lateinit var profileStandLocation:TextView
    private lateinit var progressProfile:AVLoadingIndicatorView
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var profilePageLoaderLayout:FrameLayout
    private lateinit var profilePageLoader:AVLoadingIndicatorView

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.my.app.onProfileNameReceived")
        requireActivity().registerReceiver(receiver, intentFilter)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(receiver)
    }

    private inner class MyProfileBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val extras = intent.extras
            when {
                extras!!.getString("USERNAME_CHANGE_IN_PROFILE")!=null -> {
                    val profileNameData = extras.getString("USERNAME_CHANGE_IN_PROFILE")
                    profileName.text = profileNameData
                }
                extras.getString("STAND_MODE_CHANGE_IN_PROFILE")!=null -> {
                    val profileModeDataJsonMap: java.util.HashMap<*, *>? = Gson().fromJson(extras.getString("STAND_MODE_CHANGE_IN_PROFILE"),HashMap::class.java)
                    val profileModeData: java.util.HashMap<*, *>? = Gson().fromJson(Gson().toJson(profileModeDataJsonMap!!["driver"]),HashMap::class.java)
                    driverObject?.testMode= profileModeData?.get("testMode") as Boolean
                    when (driverObject?.testMode) {
                        true -> {
                            profileStandMode.text = "(${resourceLanguage?.getString(R.string.test_mode)})"
                            profileStandMode.setTextColor(resources.getColor(R.color.colorGreen))
                        }
                        else -> {
                            profileStandMode.text = "(${resourceLanguage?.getString(R.string.production_mode)})"
                            profileStandMode.setTextColor(resources.getColor(R.color.light_blue_900))
                        }
                    }
                }
                extras.getString("STAND_NOMINEE_CHANGE_IN_PROFILE")!=null -> {
                    val profileModeDataJsonMap: java.util.HashMap<*, *>? = Gson().fromJson(extras.getString("STAND_NOMINEE_CHANGE_IN_PROFILE"),HashMap::class.java)
                    val profileModeData = Gson().fromJson(Gson().toJson(profileModeDataJsonMap!!["driver"]),NomineeNumberModel::class.java)
                    driverObject?.standRep= ((profileModeData.standRep).toLong()).toString().toLong()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        profileView=view
        view.findViewById<TextView>(R.id.title_profile_page).text= resourceLanguage?.getString(R.string.profile)
        viewPager =view.findViewById(R.id.tab_view_profile)
        tabLayout =view.findViewById(R.id.tab_layout_profile)
        profilePic=view.findViewById(R.id.profile_image)
        profileName=view.findViewById(R.id.profileName)
        profileNumber=view.findViewById(R.id.profileNumber)
        profileStand=view.findViewById(R.id.profileStand)
        profileStandMode=view.findViewById(R.id.profileStandMode)
        profileStandNominee=view.findViewById(R.id.contact_nominee_profile)
        profileStandLocation=view.findViewById(R.id.profileStandLocation)
        changePic=view.findViewById(R.id.changeProfilePicButton)
        progressProfile=view.findViewById(R.id.progress_changing)
        profilePageLoaderLayout=view.findViewById(R.id.profile_loader_layout)
        profilePageLoader=view.findViewById(R.id.profile_loader)

        progressProfile.hide()
        pageLoader(false)

        when(driverObject){
            null->GetDriverInfo().execute()
            else->setData()
        }

        changePic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 42)
                }
                else{
                    Toast.makeText(context, resourceLanguage?.getString(R.string.Permission_denied), Toast.LENGTH_SHORT).show()
                }
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, 42)
            }
        }
        view.findViewById<ImageView>(R.id.setting).setOnClickListener {
            startActivity(Intent(requireActivity(),SettingsActivity::class.java))
        }
    }

    private fun pageLoader(isLoading:Boolean){
        when(isLoading){
            true->{
                profilePageLoader.show()
                profilePageLoaderLayout.visibility=View.VISIBLE
            }
            else->{
                profilePageLoader.hide()
                profilePageLoaderLayout.visibility=View.INVISIBLE
            }
        }
    }

    private fun setData(){
        viewPager.adapter= ProfilePageAdapter(this)
        TabLayoutMediator(tabLayout,viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = resourceLanguage?.getString(R.string.edit)
                1 ->tab.text = resourceLanguage?.getString(R.string.history)
            }
        }.attach()
        if(driverObject?.imageUrl!!.compareTo("no image")==0){
            profilePic.setImageDrawable(resources.getDrawable(R.drawable.dialogbox_userpic))
        }
        else{
            Picasso.get().load(driverObject?.imageUrl).into(profilePic)
        }
        profileName.text= driverObject?.username
        profileStand.text= driverObject?.standName
        profileNumber.text= "(${driverObject?.phone})"
        when (driverObject?.testMode) {
            true -> {
                profileStandMode.text = "(${resourceLanguage?.getString(R.string.test_mode)})"
                profileStandMode.setTextColor(resources.getColor(R.color.colorGreen))
            }
            else -> {
                profileStandMode.text = "(${resourceLanguage?.getString(R.string.production_mode)})"
                profileStandMode.setTextColor(resources.getColor(R.color.light_blue_900))
            }
        }
        profileStandNominee.text= resourceLanguage?.getString(R.string.call_nominee)
        profileStandNominee.setOnClickListener {
            checkPermissionAndCall(driverObject?.standRep.toString())
        }
        profileStandLocation.text= driverObject?.standLandMark
    }

    private fun checkPermissionAndCall(number:String) {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CALL_PHONE)) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 42)
            }
            else{
                Toast.makeText(context, resourceLanguage?.getString(R.string.Permission_denied),Toast.LENGTH_SHORT).show()
            }
        } else {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
            startActivity(intent)
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetDriverInfo : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            pageLoader(true)
        }
        override fun doInBackground(vararg params: String?): String {
            return try{
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewDriver/${user?.uid}"
                val request: Request = Request.Builder().url(url).build()
                val response=OkHttpClient().newCall(request).execute()
                response.body()?.string().toString()
            }catch(e: Exception){
                "NO_READY"
            }
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result?.compareTo("NO_READY")==0){
                Toast.makeText(context,resourceLanguage?.getString(R.string.no_ready),Toast.LENGTH_SHORT).show()
            }
            else{
                pageLoader(false)
                driverObject = Gson().fromJson(result, FireDriverModel::class.java)
                setData()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK &&requestCode == 42&&null != data?.data) {
            profilePic.setImageURI(data.data)
            progressProfile.show()
            val bitMapData = (profilePic.drawable as BitmapDrawable).bitmap
            val ref = storageReference.child("AutoDriver").child("ProfilePics/"+ user?.uid+".jpg")
            val stream= ByteArrayOutputStream()
            bitMapData.compress(Bitmap.CompressFormat.JPEG, 20, stream)
            val picData = stream.toByteArray()
            val uploadTask: UploadTask = ref.putBytes(picData)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    FirebaseFunctions.getInstance().getHttpsCallable("UpdateDriver").call(mapOf("userID" to user!!.uid,"key" to "imageUrl","value" to downloadUri.toString())).addOnCompleteListener { isChanged ->
                        when (!isChanged.isSuccessful) {
                            true -> {
                                Toast.makeText(context, resourceLanguage!!.getString(R.string.upload_profile_image_failed), Toast.LENGTH_SHORT).show()
                                Picasso.get().load(driverObject?.imageUrl).into(profilePic)
                            }
                            else-> {
                                driverObject?.imageUrl=downloadUri.toString()
                                progressProfile.hide()
                                Toast.makeText(context, resourceLanguage!!.getString(R.string.uploaded), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, resourceLanguage!!.getString(R.string.upload_profile_image_failed), Toast.LENGTH_SHORT).show()
                    Picasso.get().load(driverObject?.imageUrl).into(profilePic)
                }
            }
        }
    }
}