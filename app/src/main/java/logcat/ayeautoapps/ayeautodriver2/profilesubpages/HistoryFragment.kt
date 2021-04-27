package logcat.ayeautoapps.ayeautodriver2.profilesubpages

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.adapters.HistoryAdapter
import logcat.ayeautoapps.ayeautodriver2.models.HistoryModel
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HistoryFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {

    private var historyList=ArrayList<HistoryModel>()

    private val user= FirebaseAuth.getInstance().currentUser

    private lateinit var historyRecyclerView:RecyclerView
    private lateinit var warningLayout:LinearLayout
    private lateinit var warningLayoutText:TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyRecyclerView =view.findViewById(R.id.travelListView)
        warningLayout=view.findViewById(R.id.noDataDisplayLayout_History)
        warningLayoutText=view.findViewById(R.id.waringText_History)
        swipeRefreshLayout=view.findViewById(R.id.swipe_container)
        swipeRefreshLayout.post {
            GetDriverHistory().execute()
        }
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    @Suppress("DEPRECATION")
    @SuppressLint("StaticFieldLeak")
    private inner class GetDriverHistory : AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            warningLayout.visibility=View.GONE
            swipeRefreshLayout.isRefreshing = true
            super.onPreExecute()
        }
        override fun doInBackground(vararg params: String?): String {
            return try{
                val url = "https://us-central1-auto-pickup-apps.cloudfunctions.net/ViewDriverHistory/${user?.uid}"
                val request: Request = Request.Builder().url(url).build()
                val response= OkHttpClient().newCall(request).execute()
                response.body()?.string().toString()
            }catch (e: Exception){
                "NO_READY"
            }
        }
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            swipeRefreshLayout.isRefreshing = false
            if(result?.compareTo("NO_READY")==0){
                Toast.makeText(
                    context,
                    resourceLanguage?.getString(R.string.no_ready),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else{
                val allTravelHistory: java.util.HashMap<*, *>? = Gson().fromJson(result, HashMap::class.java)
                if(historyList.isNotEmpty()){ historyList.clear() }
                allTravelHistory?.keys?.forEach{
                    val eachTravelHistory: java.util.HashMap<*, *>? = Gson().fromJson(Gson().toJson(allTravelHistory[it.toString()]), HashMap::class.java)
                    if(eachTravelHistory!!["cusName"].toString().compareTo("demoName")!=0){
                        historyList.add(
                            HistoryModel(
                                eachTravelHistory["cusName"].toString(),
                                eachTravelHistory["destination"].toString().split("_").first(),
                                eachTravelHistory["destination"].toString().split("_").last(),
                                eachTravelHistory["amount"].toString(),
                                it.toString().substring(5, 15),
                                getTime(it.toString().substring(16, 21)),
                               false
                            )
                        )
                    }
                }
                if(historyList.isEmpty()){
                    warningLayout.visibility=View.VISIBLE
                    warningLayoutText.text= resourceLanguage?.getString(R.string.warning_text_history)
                }
                else{
                    val displayList = historyList.sortedBy { it.history_date }
                    displayList.forEach {
                        if(displayList.indexOf(it)!=0){
                            it.isHeading = it.history_date != displayList[displayList.indexOf(it)-1].history_date
                        }
                        else{
                            it.isHeading=true
                        }
                    }
                    warningLayout.visibility=View.GONE
                    warningLayoutText.text= resourceLanguage?.getString(R.string.warning_text_history)
                    historyRecyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
                    val adapter= HistoryAdapter(displayList)
                    historyRecyclerView.adapter = adapter
                }
            }
        }
    }

    private fun getTime(time:String):String{
        val splitTime=time.split(":")
        return when {
            Integer.parseInt(splitTime[0]) > 12 -> {
                "${Integer.parseInt(splitTime[0])-12}:${splitTime[1]} PM"
            }
            else -> {
                "${splitTime[0]}:${splitTime[1]} AM"
            }
        }
    }

    override fun onRefresh() {
        GetDriverHistory().execute()
    }
}