package logcat.ayeautoapps.ayeautodriver2.models

import android.content.res.Resources
import android.graphics.Bitmap
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.PhoneAuthProvider

class StandModel(val standName:String,val standLandMark:String,val nomineeNumber:String,val testMode:Boolean,val members:Map<String,Number>,val callNominee:(String)->Unit,val confirmFunc:(Map<String,Number>,String,String,String,Boolean)->Unit)

class HistoryModel(val historyCusName:String, val history_from:String,val history_to:String,val history_amount:String,val history_date:String,val history_date_and_time:String,var isHeading:Boolean)

// data pass from sign Up page to verification Page
var userData= mutableMapOf<String,Any>()
var code:String = "no code"

// object of profile data

var locationProvided:Boolean=false

var driverObject: FireDriverModel?=null
var currentCustomerObject: FireCurrentCustomerModel?=null

var resourceLanguage:Resources?=null

lateinit var hireChecker: SwitchCompat