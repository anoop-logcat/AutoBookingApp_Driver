package logcat.ayeautoapps.ayeautodriver2.models

data class FireStandModel(val latitude:Number,val longitude:Number,val standName:String,val landMark:String,val testMode:Boolean,val city:String,val standRep:Number,val members:Map<String,Number>)

data class FireDriverModel(var age:String, var autoNumber:String, var workingTime:String, var imageUrl:String,val currentLatitude:Number,val currentLongitude:Number, var isHiring:Boolean, var phone:String, val standLandMark:String, val standName:String, var testMode:Boolean, var standRep:Number, val token:String, var username:String)

data class FireCustomerModel(val from:String,val customerNumber:String,val customerName:String,val customerLocation:String,val customerDate:String,val cusLatitude:Number,val cusLongitude:Number)

data class FireCurrentCustomerModel(var status:String, var fare:String,val payed:Boolean, var CustomerData: Map<String, Any>)

data class NomineeNumberModel(val standRep:Number)