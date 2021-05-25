package com.example.rbgimageprocessing

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rbgimageprocessing.models.AnalyzedRBGData

class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    var data: List<AnalyzedRBGData> = DummyData.getDummyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.color_list_item, parent, false)

        return ColorViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(data[position])
    }

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val background = itemView.findViewById<View>(R.id.ColorBox)
        private val percentageLabel = itemView.findViewById<TextView>(R.id.ColorPercentage)
        private val rgbLabel = itemView.findViewById<TextView>(R.id.ColorText)

        @SuppressLint("SetTextI18n")
        fun bind(data: AnalyzedRBGData?) {
            data?.let {
                background.setBackgroundColor(
                    Color.rgb(it.color.r,
                        it.color.g,
                        it.color.b))

                percentageLabel.text = "${it.percentageFromImage}%"
                rgbLabel.text = "R: ${it.color.r}, G: ${it.color.g}, B: ${it.color.b}"
            }
        }
    }
}