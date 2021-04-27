package logcat.ayeautoapps.ayeautodriver2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import logcat.ayeautoapps.ayeautodriver2.R
import logcat.ayeautoapps.ayeautodriver2.models.StandModel
import logcat.ayeautoapps.ayeautodriver2.models.resourceLanguage


class StandAdapter(private val standList:List<StandModel>) : RecyclerView.Adapter<StandAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.cardstand, parent, false)
        return ViewHolder(v)
    }
    override fun getItemCount(): Int {
        return standList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(standList[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(eachStand: StandModel) {
            val standLandMark:TextView=itemView.findViewById(R.id.standLoc)
            val standName:TextView=itemView.findViewById(R.id.standName)
            val standMode:TextView=itemView.findViewById(R.id.standMode)
            standName.text = eachStand.standName
            standLandMark.text = eachStand.standLandMark
            when(eachStand.testMode){
                true -> {
                    standMode.text = resourceLanguage?.getString(R.string.test_mode)
                    standMode.setTextColor(itemView.resources.getColor(R.color.colorGreen))
                }
                false -> {
                    standMode.text = resourceLanguage?.getString(R.string.production_mode)
                    standMode.setTextColor(itemView.resources.getColor(R.color.light_blue_900))
                }
            }
            itemView.findViewById<Button>(R.id.callNominee).text= resourceLanguage?.getString(R.string.call_nominee)
            itemView.findViewById<Button>(R.id.callNominee).setOnClickListener {
                eachStand.callNominee(eachStand.nomineeNumber)
            }
            itemView.findViewById<Button>(R.id.standConfirm).text= resourceLanguage?.getString(R.string.yes)
            itemView.findViewById<Button>(R.id.standConfirm).setOnClickListener {
                eachStand.confirmFunc(eachStand.members,eachStand.nomineeNumber,eachStand.standName,eachStand.standLandMark,eachStand.testMode)
            }
        }
    }
}