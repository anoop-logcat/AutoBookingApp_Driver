package logcat.ayeautoapps.ayeautodriver2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import logcat.ayeautoapps.ayeautodriver2.R


class SliderAdapter(private val sliderImageList:List<Int>) : RecyclerView.Adapter<SliderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.sliderview, parent, false)
        return ViewHolder(v)
    }
    override fun onBindViewHolder(holder: SliderAdapter.ViewHolder, position: Int) {
        holder.bindItems(sliderImageList[position])
    }
    override fun getItemCount(): Int {
        return sliderImageList.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(sliderImageList: Int) {
            val image:ImageView = itemView.findViewById(R.id.sliderImageView)
            val tipTextView:TextView=itemView.findViewById(R.id.tipTextView)
            when (sliderImageList) {
                R.drawable.tip1 -> tipTextView.text="Hey, Welcome to Taksowka Rickshaw Booking Platform"
                R.drawable.tip2 -> tipTextView.text="Get the live location of customer and contact them"
                R.drawable.tip3 -> tipTextView.text="Travel history of your customers been shown"

            }
            image.setImageResource(sliderImageList)

        }
    }
}