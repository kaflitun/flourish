package com.example.flourish

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flourish.adapter.AdapterOptions
import com.example.flourish.databinding.FragmentManagePlantBinding

// Fragment to display options to manage a plant such as watering schedule, fertilizing schedule, and others
// User can navigate to the respective fragment by clicking on the option
class ManagePlantFragment : Fragment(), AdapterOptions.RecyclerViewClickListener {

    private var _binding: FragmentManagePlantBinding? = null
    private val binding: FragmentManagePlantBinding get() = _binding!!
    private lateinit var optionsList : ArrayList<String>
    private lateinit var adapterOptions: AdapterOptions
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment
        _binding = FragmentManagePlantBinding.inflate(inflater, container, false)
        return _binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set the back button to navigate to the ViewPlantFragment
        binding.backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ViewPlantFragment())
                commit()
            }
        }
        // Get the options list from the resources
        optionsList = resources.getStringArray(R.array.manage_plant_options).toCollection(ArrayList())

        // Set adapter for the recycler view
        adapterOptions = AdapterOptions(optionsList,this)

        // Set layout manager for the recycler view
        recyclerView = binding.recyclerView
        recyclerView.adapter = adapterOptions
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

    }

    // Function to handle click events on the recycler view items and navigate to the respective fragment
    override fun onItemClick(position: Int) {
        when(optionsList[position]) {
            "Watering Schedule" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, WateringScheduleFragment())
                    commit()
                }
            }
            "Fertilizing Schedule" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, FertilisingScheduleFragment())
                    commit()
                }
            }
            "Plant Health Parameters" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, PlantParamsFragment())
                    commit()
                }
            }
            "Plant Health History" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, PlantHealthHistoryFragment())
                    commit()
                }
            }
            "Plant Disease" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, ViewPlantDiseaseFragment())
                    commit()
                }
            }
            "Plant Care Recommendations" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, PlantCareRecomFragment())
                    commit()
                }

            }
            "Plant Disease Search" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    remove(this@ManagePlantFragment)
                    commit()
                }
                startActivity(Intent(requireActivity(), SearchActivity::class.java))
            }
            "Microcontroller Connection" -> {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.fragment_container_view, MicrocontrollerConnection())
                    commit()
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Set binding to null when view is destroyed to avoid memory leaks
        _binding = null
    }
}