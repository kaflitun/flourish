package com.example.flourish.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.flourish.R

// AdapterParams class is a BaseAdapter class to display the plant health parameters
class AdapterParams(
    private val dataList: HashMap<String, String>,
    private val context: Context
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null
    private lateinit var paramName: TextView
    private lateinit var paramValue: TextView

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    // Function to create a new view for each item referenced by the Adapter
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var convertView = convertView

        if (layoutInflater == null) {
            layoutInflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        }
        if (convertView == null) {
            convertView = layoutInflater!!.inflate(R.layout.plant_param_layout, null)
        }
        paramName = convertView!!.findViewById(R.id.param_name)
        paramValue = convertView!!.findViewById(R.id.param_value)
        val currentItemKey = dataList.keys.elementAt(position)
        paramName.text = currentItemKey
        paramValue.text = dataList[currentItemKey]
        return convertView
    }
}