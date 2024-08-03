package com.example.flourish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.adapter.AdapterParams
import com.example.flourish.databinding.FragmentPlantParamsBinding
import com.example.flourish.db.Repository
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import org.mongodb.kbson.ObjectId

// Fragment to display the health parameters of a plant
// The health parameters are retrieved from the database and displayed in the grid view
class PlantParamsFragment : Fragment() {

    private var _binding: FragmentPlantParamsBinding? = null
    private val binding: FragmentPlantParamsBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository: Repository<Plant>
    private lateinit var plantRecentParams: HealthParameters
    private lateinit var plantParamsName: ArrayList<String>
    private lateinit var plant: Plant
    private lateinit var keyValueParams: HashMap<String, String>
    private lateinit var adapterParams: AdapterParams
    private lateinit var gridView: GridView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get MainViewModel and Repository instances
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        plantRepository = Repository(Plant::class)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment and return the view
        _binding = FragmentPlantParamsBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set back button click listener to navigate to the ManagePlantFragment
        binding.backBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }

        // Get a plant id from the MainViewModel
        plantId = ObjectId(mainViewModel.plantId)

        try {
            // Retrieve a plant from the database using the plant id
            plant = plantRepository.getById(plantId)!!
            Log.i("PlantParamsFragment", "Plant found: ${plant.name}")
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Plant not found", Toast.LENGTH_SHORT).show()
            Log.e("PlantParamsFragment", "Plant retrieving error ${e.message}")
        }

        // Check if the plant has health parameters
        if (plant.healthParameters.isEmpty()) {
            // Display a toast message if no health parameters are found
            Toast.makeText(requireContext(), "No health parameters found", Toast.LENGTH_SHORT)
                .show()
            Log.i("PlantParamsFragment", "No health parameters found")
        } else {
            Log.i("PlantParamsFragment", "Health parameters found")
            // Get the most recent health parameters of the plant
            plantRecentParams = plant.healthParameters.last()
            // Get plant parameters name from the resources file and store them in an array list
            plantParamsName =
                resources.getStringArray(R.array.plant_params).toCollection(ArrayList())
            // Set the grid view adapter to display the health parameters
            gridView = binding.gridViewParams
            // Create a hash map to store the health parameters and their values for the grid view adapter
            keyValueParams = HashMap()
            for (param in plantParamsName) {
                keyValueParams[param] = when (param) {
                    "Nitrogen" -> plantRecentParams.nitrogen.toString()
                    "Phosphorus" -> plantRecentParams.phosphorus.toString()
                    "Potassium" -> plantRecentParams.potassium.toString()
                    "PH" -> plantRecentParams.ph.toString()
                    "Humidity" -> plantRecentParams.humidity.toString()
                    "Temperature" -> plantRecentParams.temperature.toString()
                    "Light" -> plantRecentParams.lightLevel.toString()
                    else -> "N/A"
                }
            }

            // Set the grid view adapter with the health parameters and their values for the plant
            adapterParams = AdapterParams(keyValueParams, requireContext())
            gridView.adapter = adapterParams

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }
}