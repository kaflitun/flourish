package com.example.flourish.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flourish.R
import com.example.flourish.helper.DateTimeHandler
import com.example.flourish.model.Plant

// AdapterPlant class is a RecyclerView adapter class to display the plant list
class AdapterPlant(
    private val dataList: ArrayList<Plant>,
    private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<AdapterPlant.ViewHolderClass>() {

    // Function to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.plant_layout, parent, false)
        return ViewHolderClass(itemView)
    }

    // Function to bind the data to the ViewHolder
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        if (currentItem.image.isNotEmpty()) {
            holder.plantImage.setImageBitmap(BitmapFactory.decodeFile(currentItem.image))
        }

        if (currentItem.name.length > 18) {
            holder.plantName.text = currentItem.name.substring(0, 18) + ".."
        } else {
            holder.plantName.text = currentItem.name
        }

        if (currentItem.healthStatus.isNotEmpty()) {
            holder.healthStatus.text = currentItem.healthStatus
        } else {
            holder.healthStatus.text = "No health status available"
        }

        if (currentItem.wateringSchedule != null) {
            if (currentItem.wateringSchedule!!.weekDays.isNotEmpty()) {
                val res =
                    DateTimeHandler.getDayOfWeekForWateringSchedule(currentItem.wateringSchedule!!.weekDays)
                holder.wateringSchedule.text = "Watering: $res"
            }
        }

        if (currentItem.fertilisingSchedule != null) {
            if (currentItem.fertilisingSchedule!!.date != "") {
                val date = currentItem.fertilisingSchedule!!.date
                val res = DateTimeHandler.getDateForFertilisingSchedule(date)
                holder.fertilisingSchedule.text = "Fertilising: $res"
            }
        }
    }

    // Function to get the item count
    override fun getItemCount(): Int {
        return dataList.size
    }

    // ViewHolderClass is a class to hold the views of the RecyclerView item
    inner class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val plantName: TextView = itemView.findViewById(R.id.plant_name)
        val plantImage: ImageView = itemView.findViewById(R.id.plant_image)
        val healthStatus: TextView = itemView.findViewById(R.id.plant_health_status)
        val wateringSchedule: TextView = itemView.findViewById(R.id.plant_watering)
        val fertilisingSchedule: TextView = itemView.findViewById(R.id.plant_fertilising)
        private val removePlantBtn: ImageView = itemView.findViewById(R.id.delete_plant_button)

        // Function to set click listener on the item view
        init {
            itemView.setOnClickListener(this)
            removePlantBtn.setOnClickListener {
                listener.onRemovePlantClick(adapterPosition)
            }
        }

        // Function to handle the click event on the item view
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }

    }

    // Interface to handle the click events on the RecyclerView item such as item click and remove plant click
    interface RecyclerViewClickListener {
        fun onItemClick(position: Int)
        fun onRemovePlantClick(position: Int)
    }
}