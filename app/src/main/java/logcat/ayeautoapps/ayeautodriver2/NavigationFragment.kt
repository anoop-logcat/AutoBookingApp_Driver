package logcat.ayeautoapps.ayeautodriver2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.gson.Gson
import com.skyfishjy.library.RippleBackground
import com.wang.avi.AVLoadingIndicatorView
import logcat.ayeautoapps.ayeautodriver2.models.*
import okhttp3.OkHttpClient
import okhttp3.Request

class NavigationFragment : Fragment() {

    private val user= FirebaseAuth.getInstance().currentUser
    private val receiver = MyBroadCastReceiver()

    private lateinit var searchingLayout: RippleBackground
    private lateinit var newCustomerLayout:RelativeLayout
    private lateinit var hiring: RelativeLayout
    private lateinit var movingSearch:ImageView
    private lateinit var cancelCustomer:Button
    private lateinit var acceptCustomer:Button
    private lateinit var loading: AVLoadingIndicatorView
    private lateinit var customerName:TextView
    private lateinit var from: TextView
    private lateinit var to: TextView
    private lateinit var customerAmount: TextView
    private lateinit var tripEndButton:Button

    private val REQUEST_CHECK_SETTINGS = 214

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.my.app.onMessageReceived")
        requireActivity().registerReceiver(receiver, intentFilter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_navigation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.appTitle).text= resourceLanguage?.getString(R.string.aye_auto)
        view.findViewById<TextView>(R.id.hiring_title_text).text= resourceLanguage?.getString(R.string.hiring_mode)
        view.findViewById<TextView>(R.id.searching_text_view).text= resourceLanguage?.getString(R.string.searching_customer)
        customerAmount = view.findViewById(R.id.customerAmount)
        hireChecker = view.findViewById(R.id.hireSwitch)
        searchingLayout = view.findViewById(R.id.search_layer)
        newCustomerLayout = view.findViewById(R.id.new_customer_layer)
        movingSearch = view.findViewById(R.id.moving_search_icon)
        cancelCustomer=view.findViewById(R.id.cancel_customer)
        cancelCustomer.text= resourceLanguage?.getString(R.string.cancel)
        acceptCustomer=view.findViewById(R.id.accept_customer)
        acceptCustomer.text= resourceLanguage?.getString(R.string.contact)
        loading=view.findViewById(R.id.progressInNav)
        customerName=view.findViewById(R.id.customerName)
        from=view.findViewById(R.id.customerLocation1)
        to=view.findViewById(R.id.customerLocation2)
        hiring=view.findViewById(R.id.hirelayout)
        tripEndButton=view.findViewById(R.id.trip_ends_button)
        tripEndButton.text= resourceLanguage?.getString(R.string.trip_ends)
        view.findViewById<ImageView>(R.id.reloadData).setOnClickListener {
            GetDriverInfo().execute(user?.uid)
        }
        when(currentCustomerObject==null){
            true -> GetDriverInfo().execute(user?.uid)
            else-> settingDataObjects("NO_JSON_STRING")
        }
    }

    private fun loadingData(){
        searchingLayout.visibility=View.INVISIBLE
        newCustomerLayout.visibility=View.INVISIBLE
        hiring.visibility=View.INVISIBLE
        loading.show()
        searchingLayout.stopRippleAnimation()
    }
    private  fun searchingCustomer(){
        newCustomerLayout.visibility=View.INVISIBLE
        loading.hide()
        searchingLayout.visibility=View.VISIBLE
        hiring.visibility=View.VISIBLE
        searchingLayout.startRippleAnimation()
    }
    private fun findCustomer(){
        loading.hide()
        searchingLayout.visibility=View.INVISIBLE
        hiring.visibility=View.VISIBLE
        from.isSelected=true
        to.isSelected=true
        newCustomerLayout.visibility=View.VISIBLE
        searchingLayout.stopRippleAnimation()
    }

    private fun notSearchingCustomer(){
        loading.hide()
        searchingLayout.visibility=View.INVISIBLE
        hiring.visibility=View.VISIBLE
        newCustomerLayout.visibility=View.INVISIBLE
        searchingLayout.stopRippleAnimation()
    }

    private inner class MyBroadCastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val state = intent.getStringExtra("CUSTOMER_DATA")
            val cusData: java.util.HashMap<*, *>? = Gson().fromJson(state, HashMap::class.java)
            currentCustomerObject = Gson().fromJson(Gson().toJson(cusData!!["customer"]), FireCurrentCustomerModel::class.java)
            settingDataObjects("NO_JSON_STRING")
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetDriverInfo : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            loadingData()
        }
        override fun doInBackground(vararg params: String?): String {
            return try{
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewDriver/${params[0]!!}"
                val url2 = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewCurrentCustomer/${params[0]!!}"
                val request:Request=Request.Builder().url(url).build()
                val request2:Request=Request.Builder().url(url2).build()
                val client = OkHttpClient()
                val response= client.newCall(request).execute()
                val response2= client.newCall(request2).execute()
                response.body()?.string().toString()+"|"+response2.body()?.string().toString()
            }catch (e: Exception){
                "NO_READY"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            settingDataObjects(result!!)
        }
    }

    private fun settingDataObjects(result: String?){
        if(result?.compareTo("NO_READY")==0){
            Toast.makeText(context, resourceLanguage?.getString(R.string.no_ready), Toast.LENGTH_SHORT).show()
        }
        else{
            if(result?.compareTo("NO_JSON_STRING")!=0){
                val driverDataJSON:String = result?.split('|')!!.first()
                val currentCustomerDataJSON:String = result.split('|').last()
                driverObject = Gson().fromJson(driverDataJSON, FireDriverModel::class.java)
                currentCustomerObject = Gson().fromJson(currentCustomerDataJSON, FireCurrentCustomerModel::class.java)
            }
            when (driverObject?.isHiring) {
                true -> {
                    hireChecker.isChecked = true
                    hireNotification(true)
                    searchingCustomer()
                    if (currentCustomerObject?.status?.compareTo("YES_CUSTOMER") == 0) {
                        val customerObject = Gson().fromJson(Gson().toJson(currentCustomerObject?.CustomerData), FireCustomerModel::class.java)
                        customerPreview(customerObject, currentCustomerObject!!.fare)
                    }
                }
                false -> {
                    hireChecker.isChecked = false
                    hireNotification(false)
                    notSearchingCustomer()
                }
            }
            hireChecker.setOnCheckedChangeListener { _, isChecked ->
                when(isChecked){
                    true -> {
                        currentLocationGenerator(executeFunc = {driverLat,driverLong->
                            FirebaseFunctions.getInstance().getHttpsCallable("ChangeHiring").call(mapOf(
                                    "hiringValue" to true,
                                    "currentLatitude" to driverLat,
                                    "currentLongitude" to driverLong,
                            )).addOnCompleteListener { isSwitched ->
                                when (isSwitched.isSuccessful) {
                                    true -> {
                                        hireNotification(true)
                                        driverObject?.isHiring = true
                                        searchingCustomer()
                                        if (currentCustomerObject?.status?.compareTo("YES_CUSTOMER") == 0) {
                                            val customerObject = Gson().fromJson(Gson().toJson(currentCustomerObject?.CustomerData), FireCustomerModel::class.java)
                                            customerPreview(customerObject, currentCustomerObject!!.fare)
                                        }
                                    }
                                    false -> {
                                        Toast.makeText(context, resourceLanguage!!.getString(R.string.failed_to_change), Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        })
                    }
                    false -> {
                        FirebaseFunctions.getInstance().getHttpsCallable("ChangeHiring").call(mapOf("hiringValue" to false)).addOnCompleteListener { isSwitched ->
                            when (isSwitched.isSuccessful) {
                                true -> {
                                    driverObject?.isHiring = false
                                    hireNotification(false)
                                    notSearchingCustomer()
                                }
                                false -> {
                                    Toast.makeText(context, resourceLanguage!!.getString(R.string.failed_to_change), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun customerPreview(customerObject: FireCustomerModel, amount: String){
        findCustomer()
        val fromLocation:String = customerObject.customerLocation.split('_').first()
        val toLocation:String = customerObject.customerLocation.split('_').last()
        customerName.text=customerObject.customerName
        from.text=fromLocation
        to.text=toLocation
        customerAmount.text="$amount ${resourceLanguage!!.getString(R.string.rupees)}"
        cancelCustomer.setOnClickListener {
            removeCustomer("CANCEL_CUSTOMER", cancelCustomer)
        }
        tripEndButton.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(resourceLanguage!!.getString(R.string.warning))
            builder.setMessage(resourceLanguage!!.getString(R.string.are_you_sure))
            builder.setPositiveButton(resourceLanguage!!.getString(R.string.yes)) { _, _ ->
                removeCustomer("NO_CUSTOMER",tripEndButton)
            }
            builder.setNegativeButton(resourceLanguage!!.getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            builder.show()
        }
        acceptCustomer.setOnClickListener {
            val builderSingle: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            builderSingle.setTitle(resourceLanguage?.getString(R.string.contact_with))
            val arrayAdapter = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1)
            arrayAdapter.add(resourceLanguage?.getString(R.string.call_traveller))
            arrayAdapter.add(resourceLanguage?.getString(R.string.navigate_traveller))
            builderSingle.setNegativeButton(resourceLanguage?.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
            builderSingle.setAdapter(arrayAdapter) { _, which ->
                val strName = arrayAdapter.getItem(which)
                when(strName?.compareTo(resourceLanguage?.getString(R.string.call_traveller)!!)==0){
                    true -> {
                        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.CALL_PHONE)) {
                                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CALL_PHONE), 42)
                            } else {
                                Toast.makeText(context, resourceLanguage?.getString(R.string.Permission_denied), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${customerObject.customerNumber}"))
                            startActivity(intent)
                        }
                    }
                    else->{
                        val intentData = Intent(requireActivity(), MapActivity::class.java)
                        intentData.putExtra("from", customerObject.from)
                        intentData.putExtra("phone", customerObject.customerNumber)
                        intentData.putExtra("name", customerObject.customerName)
                        intentData.putExtra("amount", currentCustomerObject?.fare)
                        intentData.putExtra("location", customerObject.customerLocation)
                        intentData.putExtra("latitude", (customerObject.cusLatitude).toString())
                        intentData.putExtra("longitude", (customerObject.cusLongitude).toString())
                        startActivity(intentData)
                    }
                }
            }
            builderSingle.show()
        }
    }
    private fun removeCustomer(mapValue: String, activationButton: Button){
        activationButton.isEnabled=false
        activationButton.isClickable=false
        FirebaseFunctions.getInstance().getHttpsCallable("RemoveCustomer").call(mapOf("value" to mapValue)).addOnCompleteListener { isRemoved ->
            activationButton.isEnabled=true
            activationButton.isClickable=true
            when (isRemoved.isSuccessful) {
                true -> {
                    currentCustomerObject?.status = mapValue
                    searchingCustomer()
                }
                else -> Toast.makeText(context, resourceLanguage!!.getString(R.string.failed_to_change), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun currentLocationGenerator(executeFunc:(Double,Double)->Unit){
        locationProvided=true
           if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                hireChecker.isChecked=false
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            }
            else{
                Toast.makeText(context, resourceLanguage?.getString(R.string.Permission_denied), Toast.LENGTH_SHORT).show()
            }
        } else {
            val builder = LocationSettingsRequest.Builder().addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
            builder.addLocationRequest(LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY))
            builder.setAlwaysShow(true)
            val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(requireActivity())
            val mLocationSettingsRequest: LocationSettingsRequest = builder.build()
            val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener {
                val reqSetting = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 10000
                    fastestInterval =10000
                    smallestDisplacement = 1.0f
                }
                val locationUpdates = object : LocationCallback() {
                    override fun onLocationResult(lr: LocationResult) {
                        if(locationProvided){
                           executeFunc(lr.lastLocation.latitude,lr.lastLocation.longitude)
                        }
                        locationProvided=false
                    }
                }
                fusedLocationClient.requestLocationUpdates(reqSetting, locationUpdates, null)
            }.addOnFailureListener { e ->
                hireChecker.isChecked=false
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        rae.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
                    } catch (sie: IntentSender.SendIntentException) {
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                    }
                }
            }
        }
    }
    private fun hireNotification(mode:Boolean){
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(mode){
            val snoozeIntent = Intent(activity,HireBroadCastReceiver()::class.java)
            snoozeIntent.putExtra("HIRE_DATA","YES")
            val snoozePendingIntent:PendingIntent  = PendingIntent.getBroadcast(activity,0,snoozeIntent,PendingIntent.FLAG_UPDATE_CURRENT)
            val notificationBuilder = NotificationCompat.Builder(requireContext(), getString(R.string.hire_notification_channel_default))
                    .setSmallIcon(R.drawable.autologo)
                    .setColor(resources.getColor(R.color.colorPrimary))
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.driverr_icon))
                    .setAutoCancel(false)
                    .setNotificationSilent()
                    .setContentTitle(resourceLanguage!!.getString(R.string.hire_mode_on))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(snoozePendingIntent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(resourceLanguage!!.getString(R.string.hire_mode_description)))
                    .setOngoing(true)
                    .addAction(R.drawable.close_icon,resourceLanguage!!.getString(R.string.hire_off),snoozePendingIntent)
            notificationManager.notify(1, notificationBuilder.build())
        }
        else{
            notificationManager.cancel(1)
        }
    }
}