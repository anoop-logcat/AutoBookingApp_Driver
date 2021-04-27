package logcat.ayeautoapps.ayeautodriver2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.ayeautodriver2.adapters.StandAdapter
import logcat.ayeautoapps.ayeautodriver2.models.FireStandModel
import logcat.ayeautoapps.ayeautodriver2.models.StandModel
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage
import okhttp3.OkHttpClient

class StandSelectionFragment : Fragment(), AdapterView.OnItemClickListener {

    private var standList = ArrayList<StandModel>()
    private val districts = Array(15){""}
    private lateinit var standRecyclerView:RecyclerView
    private lateinit var phone:TextView
    private lateinit var navController: NavController
    private lateinit var noDataDisplay: LinearLayout
    private lateinit var selectionLoader:AVLoadingIndicatorView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_stand_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val districtDrop: AutoCompleteTextView = view.findViewById(R.id.districtCategory)
        phone = view.findViewById(R.id.phoneEditTextSignUp)
        navController = Navigation.findNavController(view)
        standRecyclerView=view.findViewById(R.id.standRecyclerView)
        noDataDisplay=view.findViewById(R.id.noDataDisplayLayout)
        selectionLoader=view.findViewById(R.id.selection_loader)
        standRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        standRecyclerView.setHasFixedSize(true)
        noDataDisplay.visibility=View.GONE
        selectionLoader.hide()
        view.findViewById<TextView>(R.id.stand_selection_title).text= resourceLanguage?.getString(R.string.select_your_stand)
        view.findViewById<TextView>(R.id.waringText).text= resourceLanguage?.getString(R.string.warning_text)
        view.findViewById<TextView>(R.id.warningButton).text= resourceLanguage?.getString(R.string.contact_us)
        view.findViewById<TextView>(R.id.warningButton).setOnClickListener {
            callAdmin()
        }

        districts[0] = "Thiruvananthapuram"
        districts[1] = "Kollam"
        districts[2] = "Kollam"
        districts[3] = "Alappuzha"
        districts[4] = "Ernakulam"
        districts[5] = "Idukki"
        districts[6] = "Kannur"
        districts[7] = "Kasaragod"
        districts[8] = "Kottayam"
        districts[9] = "Kozhikode"
        districts[10] = "Malappuram"
        districts[11] = "Palakkad"
        districts[12] = "Pathanamthitta"
        districts[13] = "Thrissur"
        districts[14] = "Wayanad"
        val langAdapter= ArrayAdapter(requireActivity(),R.layout.drop_list_item,districts)
        districtDrop.setAdapter(langAdapter)
        districtDrop.onItemClickListener = this
    }

    private fun callAdmin() {
        val builderSingle: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builderSingle.setTitle(resourceLanguage?.getString(R.string.contact_us))
        val arrayAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1)
        arrayAdapter.add("Logcat Solutions:7909254407")
        builderSingle.setNegativeButton(resourceLanguage?.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
        builderSingle.setAdapter(arrayAdapter) { _, which ->
            val strName = "+91"+(arrayAdapter.getItem(which))!!.split(":")[1]
            checkPermissionAndCall(strName)
        }
        builderSingle.show()
    }


    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetStandInfo : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            selectionLoader.show()
            noDataDisplay.visibility=View.GONE
            if(standList.isNotEmpty()){standList.clear()}
        }
        override fun doInBackground(vararg params: String?): String {
            val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewAutoStand/${params[0]!!}"
            val request: okhttp3.Request = okhttp3.Request.Builder().url(url).build()
            val response= OkHttpClient().newCall(request).execute()
            return response.body()?.string().toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            selectionLoader.hide()
            getStandData(result!!)
        }
    }

    private fun getStandData(response: String){
        if(response.compareTo("no data")==0){
            noDataDisplay.visibility=View.VISIBLE
        }
        else{
            val allDocMap: java.util.HashMap<*, *>? = Gson().fromJson(response,HashMap::class.java)
            for(key in allDocMap!!.keys){
                val eachDocData = Gson().fromJson(Gson().toJson(allDocMap[key]), FireStandModel::class.java)
                standList.add(StandModel(eachDocData.standName,eachDocData.landMark,((eachDocData.standRep).toLong()).toString(),eachDocData.testMode,eachDocData.members,{ nominee->
                    checkPermissionAndCall(nominee)
                },{ members,nominee,standName,standLoc,testModeValue->
                    when {
                        phone.text.isNullOrBlank() -> {
                            Toast.makeText(context, resourceLanguage?.getString(R.string.phone_field_is_empty),Toast.LENGTH_SHORT).show()
                        }
                        phone.text.toString().length!=10 -> {
                            Toast.makeText(context,resourceLanguage?.getString(R.string.phone_field_is_invalid),Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            var isPassed=false
                            for( eachMember:String in members.keys){
                                if(phone.text.toString().compareTo((members[eachMember]?.toLong()).toString())==0||phone.text.toString().compareTo(nominee)==0){
                                    isPassed=true
                                    break
                                }
                            }
                            when(isPassed){
                                true ->{
                                    val action =StandSelectionFragmentDirections.actionStandSelectionFragmentToSignUpFragment()
                                    action.standDetails="$standName,$standLoc"
                                    action.phoneNumber=phone.text.toString()
                                    action.standMode=testModeValue
                                    action.standNominee=nominee.toLong()
                                    navController.navigate(action)
                                }
                                false ->Toast.makeText(context,resourceLanguage?.getString(R.string.Failed_to_find_your_number),Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }))
            }
        }
        val adapter = StandAdapter(standList)
        standRecyclerView.adapter = adapter
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

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        GetStandInfo().execute(districts[position])
    }
}