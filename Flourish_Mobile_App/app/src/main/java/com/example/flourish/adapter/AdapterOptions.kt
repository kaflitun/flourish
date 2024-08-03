package com.example.flourish.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.flourish.R


// AdapterOptions class is a RecyclerView adapter class to display different options
class AdapterOptions(
    private val dataList: ArrayList<String>,
    private val listener: RecyclerViewClickListener
) : RecyclerView.Adapter<AdapterOptions.ViewHolderClass>() {

    // Function to create a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClass {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.manage_plant_layout, parent, false)
        return ViewHolderClass(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolderClass, position: Int) {
        val currentItem = dataList[position]
        holder.optionName.text = currentItem
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    // ViewHolderClass is a class to hold the views for each item in the RecyclerView
    inner class ViewHolderClass(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val optionName: TextView = itemView.findViewById(R.id.manage_plant_option)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(position)
            }
        }
    }

    // RecyclerViewClickListener is an interface to handle the item click event
    interface RecyclerViewClickListener {
        fun onItemClick(position: Int)
    }
}