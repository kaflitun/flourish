package com.example.flourish

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentPlantCareRecomBinding
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.helper.DateTimeHandler
import com.example.flourish.helper.GptApiRequestHandler
import com.example.flourish.model.Plant
import com.example.flourish.model.Recommendation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.mongodb.kbson.ObjectId

// Fragment to display the plant care recommendation
// The plant care recommendation is retrieved from the database and displayed
// The user can update the plant care recommendation by sending a request to OpenAI
// The updated plant care recommendation is saved in the database and displayed
class PlantCareRecomFragment : Fragment() {
    private var _binding: FragmentPlantCareRecomBinding? = null
    private val binding: FragmentPlantCareRecomBinding get() = _binding!!
    private lateinit var plantRepository : Repository<Plant>
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plantId: ObjectId
    private lateinit var plant : Plant
    private lateinit var plantRecom : Recommendation
    private var plantNewRecom : Recommendation = Recommendation()
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
        // Bind layout for this fragment
        _binding = FragmentPlantCareRecomBinding.inflate(inflater, container, false)
        return _binding?.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set back button click listener to navigate to the ManagePlantFragment
        binding.backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }

        // Get a plant id from the MainViewModel
        plantId = ObjectId(mainViewModel.plantId)

        try {
            // Get a plant from the database
            plant = plantRepository.getById(plantId)!!
            Log.i("PlantCareRecomFragment", "Plant found: ${plant.name}")
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Plant not found", Toast.LENGTH_SHORT).show()
            Log.e("PlantCareRecomFragment", "Plant retrieving error: ${e.message}")
        }
        // Check if the plant has a recommendation
        if(plant.recommendation == null){
            // Display a message if the plant has no recommendation
            binding.heading.text = "No plant recommendations found"
            // Hide the line separator
            binding.line.visibility = View.INVISIBLE
            Log.i("PlantCareRecomFragment", "No plant recommendations found")
        }
        else{
            Log.i("PlantCareRecomFragment", "Plant recommendations found")
            // Get and display the plant care recommendation
            plantRecom = plant.recommendation!!
            binding.heading.text = plantRecom.heading
            binding.description.text = plantRecom.text
            binding.date.text = plantRecom.dateTime
        }

        // If the update button is clicked, send a request to OpenAI to get the updated plant care recommendation
        binding.updateBtn.setOnClickListener{
            // Reset the plant care recommendation fields
            binding.heading.text = "Loading..."
            binding.line.visibility = View.INVISIBLE
            binding.description.text = ""
            binding.date.text = ""

            // Create a request to send to OpenAI
            val req = GptApiRequestHandler.createRequestPlantCareRecommendation(plant)
            CoroutineScope(Dispatchers.IO).launch {
                // Call function to send the image to OpenAI
                try {
                    GptApiRequestHandler.sendRequestToOpenAIWithCoroutines(req) { result ->
                        // Process the result on the main thread
                        requireActivity().runOnUiThread {
                            // Get the response from the result and parse to the JSON object
                            val response = JSONObject(result)
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")

                            // Set the updated plant care recommendation from OpenAI response
                            plantNewRecom.heading = "Plant Care Recommendation for ${plant.name}"
                            plantNewRecom.dateTime = DateTimeHandler.getCurrentDateTimeFormatted()
                            plantNewRecom.text = response
                            Log.i("PlantCareRecomFragment", "Plant recommendation received")

                            // Save the updated plant care recommendation in the database using coroutines and Realm
                            CoroutineScope(Dispatchers.IO).launch {
                                // Write the updated plant care recommendation to the database
                                Database.realm.write {
                                    // Find the latest plant in the database
                                    val managedPlant = findLatest(plant)
                                    // Set the updated plant care recommendation
                                    managedPlant?.recommendation = plantNewRecom
                                    Log.i("PlantCareRecomFragment", "Plant recommendation saved")
                                }
                            }

                            // Display the updated plant care recommendation
                            binding.line.visibility = View.VISIBLE
                            binding.heading.text = plantNewRecom.heading
                            binding.description.text = plantNewRecom.text
                            binding.date.text = plantNewRecom.dateTime

                        }
                    }
                }
                catch (e: Exception){
                    Log.e("PlantCareRecomFragment", "Request error: ${e.message}")
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }
}