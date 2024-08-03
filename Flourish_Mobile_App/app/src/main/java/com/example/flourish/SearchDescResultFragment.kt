package com.example.flourish

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentSearchDescriptionResultBinding
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.enum.PlantHealthEnum
import com.example.flourish.helper.GptApiRequestHandler
import com.example.flourish.helper.Image
import com.example.flourish.helper.JsonToObjectParser
import com.example.flourish.model.Disease
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.mongodb.kbson.BsonObjectId

// Fragment for displaying the search result description for a plant or a disease
// It sends a request to the OpenAI API to get the description of the plant or the disease
// It also sends a request to get the parameters of the plant
// It saves the plant or the disease to the database in case the user wants to save it
class SearchDescResultFragment : Fragment() {
    private var _binding: FragmentSearchDescriptionResultBinding? = null
    private val binding: FragmentSearchDescriptionResultBinding get() = _binding!!
    private lateinit var searchModel: SearchModel
    private lateinit var searchType: String
    private lateinit var name: String
    private lateinit var imgPath: String
    private lateinit var plantRepo: Repository<Plant>
    private lateinit var req: String
    private lateinit var paramReq: String
    private var plant: Plant = Plant()
    private var plantId: String = ""
    private var healthParameters: HealthParameters = HealthParameters()
    private var disease: Disease = Disease()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the search type and the plant ID from the ViewModel
        searchModel = ViewModelProvider(requireActivity())[SearchModel::class.java]
        searchType = searchModel.searchType
        plantId = searchModel.plantId
        // Create requests for the OpenAI API based on the search type
        arguments?.let {
            name = it.getString("name", null).toString()
            req = if (searchType == "disease") {
                GptApiRequestHandler.createRequestPlantDiseaseDescription(name)
            } else {
                GptApiRequestHandler.createRequestPlantDescription(name)
            }
            // Get the image path from the arguments
            imgPath = it.getString("imgPath", null)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment and return the root view
        _binding = FragmentSearchDescriptionResultBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("SearchDescResultFragment", "Search type: $searchType")

        // If image path is not empty, set the image view to the image from the path
        if (imgPath.isNotEmpty()) {
            Log.i("SearchDescResultFragment", "Image path not empty: $imgPath")
            val img = BitmapFactory.decodeFile(imgPath)
            binding.img.setImageBitmap(img)
        }

        // On Cancel button click, delete the image from the storage and navigate to the previous screen
        binding.btnCancel.setOnClickListener {
            Image.deleteImageFromStorage(imgPath)

            requireActivity().supportFragmentManager.beginTransaction().apply {
                remove(SearchDescResultFragment())
                commit()
            }

            // Navigate to the previous screen based on the search type (plant or disease)
            if (searchType == "disease") {
                startActivity(Intent(requireContext(), PlantActivity::class.java))
            } else {
                startActivity(Intent(requireContext(), HomeActivity::class.java))
            }
        }

        // On Select button click, save the plant or the disease to the database
        binding.btnSelect.setOnClickListener {
            // If the plant is not found, show a toast message and return
            // Because there is no plant to save to the database
            // The user should retry the search
            if (binding.name.text == "No plant found") {
                Log.i("SearchDescResultFragment", "No plant found")
                Toast.makeText(
                    requireContext(),
                    "Nothing found. Please, try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // Create a new plant or a new disease object
            plantRepo = Repository(Plant::class)

            // If the image path is not empty, set the image path to the plant or the disease
            if (imgPath.isNotEmpty()) {
                if (searchType == "plant") {
                    plant.image = imgPath
                } else {
                    disease.image = imgPath
                }
            }

            // Save the plant or the disease to the database
            if (searchType == "plant") {
                // Set the health status to healthy and save the plant to the database
                plant.healthStatus = PlantHealthEnum.HEALTHY.healthStats
                plantRepo.insert(plant)
                Log.i("SearchDescResultFragment", "Plant saved: ${plant.name}")
            } else {
                // Save the disease to the database
                try {
                    // Get the plant from the database based on the plant ID
                    plant = plantRepo.getById(BsonObjectId(plantId))!!
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Disease is not saved", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("SearchDescResultFragment", "Error retrieving a plant: ${e.message}")
                }
                // If the plant is found, set the health status to diseased and save the disease to the database
                CoroutineScope(Dispatchers.Main).launch {
                    if (plantId.isNotEmpty()) {
                        // Save the disease to the database using coroutines and the IO dispatcher
                        withContext(Dispatchers.IO) {
                            // Write the disease to the database
                            Database.realm.write {
                                // Find the latest plant in the database
                                val managedPlant = findLatest(plant)
                                // Set the health status to diseased and save the disease to the plant
                                managedPlant?.healthStatus = PlantHealthEnum.DISEASED.healthStats
                                managedPlant?.disease = disease
                                Log.i(
                                    "SearchDescResultFragment",
                                    "Disease saved: ${disease.heading}"
                                )
                            }
                        }
                    }
                }
            }
            requireActivity().supportFragmentManager.beginTransaction().apply {
                remove(SearchDescResultFragment())
                commit()
            }
            startActivity(Intent(requireContext(), HomeActivity::class.java))
        }

        // Send a request to the OpenAI API to get the description of the plant or the disease
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Send the request to the OpenAI API using coroutines and the IO dispatcher
                // Depending on the search type, the request is sent to get the plant or the disease description
                GptApiRequestHandler.sendRequestToOpenAIWithCoroutines(req) { result ->
                    // Process the result on  the main thread
                    requireActivity().runOnUiThread {
                        // Get the response from the OpenAI API and parse the JSON
                        val response = JSONObject(result)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content")
                        try {
                            // Parse the JSON response to get the plant or the disease
                            val json = JSONObject(response)
                            // If the search type is plant, parse the plant
                            if (searchType == "plant") {
                                // If the plant is not found, show a message
                                if (json.getString("plant") == "false") {
                                    binding.name.text = "No plant found"
                                    Log.i(
                                        "SearchDescResultFragment",
                                        "No plant found in the response JSON"
                                    )
                                }
                                // If the plant is found, parse the plant
                                else {
                                    Log.i(
                                        "SearchDescResultFragment",
                                        "Plant found in the response JSON"
                                    )
                                    // Parse the plant JSON to the plant object using the JSON parser class
                                    plant = JsonToObjectParser.parsePlantJsonToObject(json)
                                    // Create a request to get the plant parameters based on the plant name
                                    paramReq =
                                        GptApiRequestHandler.createRequestPlantParams(plant.name)

                                    // Set the plant name, description, and the plant parameters
                                    binding.name.text = plant.name
                                    binding.description.text = plant.description
                                    binding.text1.text = "Watering:  \n" + plant.requiredWatering
                                    binding.text2.text = "Light:   \n" + plant.requiredLight
                                    binding.text3.text = "Soil:   \n" + plant.requiredSoilParameters
                                    binding.text4.text = "Feeding:   \n" + plant.requiredFeeding

                                    // Second request to get the plant parameters
                                    CoroutineScope(Dispatchers.IO).launch {
                                        // Send the request to the OpenAI API using coroutines and the IO dispatcher
                                        GptApiRequestHandler.sendRequestToOpenAIWithCoroutines(
                                            paramReq
                                        ) { result ->
                                            // Process the result on  the main thread using runOnUiThread
                                            requireActivity().runOnUiThread {
                                                val responseParams = JSONObject(result)
                                                    .getJSONArray("choices")
                                                    .getJSONObject(0)
                                                    .getJSONObject("message")
                                                    .getString("content")
                                                try {
                                                    // Parse the JSON response to get the plant parameters
                                                    val jsonParams = JSONObject(responseParams)
                                                    // Parse the plant parameters JSON to the plant parameters object using the JSON parser class
                                                    healthParameters =
                                                        JsonToObjectParser.parsePlantParamsJsonToObject(
                                                            jsonParams
                                                        )
                                                    // Set the plant parameters to the plant object
                                                    plant.requiredHealthParameters =
                                                        healthParameters
                                                    Log.i(
                                                        "SearchDescResultFragment",
                                                        "Plant parameters found in the response JSON"
                                                    )
                                                } catch (e: Exception) {
                                                    Log.e(
                                                        "SearchDescResultFragment",
                                                        "Error parsing JSON: ${e.message}"
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // If the search type is disease, parse the disease
                                if (json.getString("plant_disease") == "false") {
                                    // If the disease is not found, show a message
                                    binding.name.text = "No plant disease found"
                                    Log.i(
                                        "SearchDescResultFragment",
                                        "No plant disease found in the response JSON"
                                    )
                                } else {
                                    Log.i(
                                        "SearchDescResultFragment",
                                        "Plant disease found in the response JSON"
                                    )
                                    // Parse the disease JSON to the disease object using the JSON parser class
                                    disease = JsonToObjectParser.parsePlantDiseaseJsonToObject(json)
                                    // Set the disease name, description, symptoms, causes, and treatment on the UI
                                    binding.name.text = disease.heading
                                    binding.description.text = disease.description
                                    binding.text1.text = "Symptoms:  \n" + disease.symptoms
                                    binding.text2.text = "Causes:   \n" + disease.causes
                                    binding.text3.text = "Treatment:   \n" + disease.treatment
                                }
                            }
                        } catch (e: JSONException) {
                            Log.e("SearchDescResultFragment", "Error parsing JSON: ${e.message}")
                        } catch (e: Exception) {
                            Log.e("SearchDescResultFragment", "Error: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchDescResultFragment", "Error sending request: ${e.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view to avoid memory leaks
        _binding = null
    }
}