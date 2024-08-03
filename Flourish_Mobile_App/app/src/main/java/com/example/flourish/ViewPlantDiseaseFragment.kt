package com.example.flourish

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentViewPlantDiseaseBinding
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.enum.PlantHealthEnum
import com.example.flourish.model.Disease
import com.example.flourish.model.Plant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

// Fragment to view the plant disease details for a selected plant from the manage plant fragment
// The plant disease details are displayed in the text views and image view
// The user can delete the plant disease if it exists
// The plant health status is updated to healthy if the plant disease is deleted
class ViewPlantDiseaseFragment : Fragment() {
    private var _binding: FragmentViewPlantDiseaseBinding? = null
    private val binding: FragmentViewPlantDiseaseBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plant: Plant
    private lateinit var plantDisease: Disease
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository: Repository<Plant>
    private lateinit var diseaseRepository: Repository<Disease>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the main view model from the activity
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        // Create a new instance of the plant repository
        plantRepository = Repository(Plant::class)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the view and return it to the fragment manager to display it on the screen
        _binding = FragmentViewPlantDiseaseBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the plant id from the main view model and convert it to an ObjectId
        plantId = ObjectId(mainViewModel.plantId)

        // Set the on click listener for the back button to remove the current fragment and go back to the manage plant fragment
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }
        // Set the on click listener for the delete button to show an alert dialog to confirm the deletion of the plant disease
        binding.deleteBtn.setOnClickListener {
            showAlertDialog(plant)
        }

        try {
            // Get the plant from the plant repository using the plant id
            plant = plantRepository.getById(plantId)!!
            Log.i("ViewPlantDiseaseFragment", "Plant found: ${plant.name}")

            if (plant.disease != null) {
                // Get the plant disease from the plant if it exists
                plantDisease = plant.disease!!
                // Set the text views to display the plant disease information
                binding.txtPlantDiseaseName.text = plantDisease.heading
                binding.txtDescription.text = plantDisease.description
                binding.txtSymptoms.text = "Symptoms: \n" + plantDisease.symptoms
                binding.txtCauses.text = "Causes: \n" + plantDisease.causes
                binding.txtTreatment.text = "Treatment: \n" + plantDisease.treatment

                // Set the image view to display the plant disease image if it exists
                if (plantDisease.image.isNotEmpty()) {
                    binding.imgPlantDiseaseImage.setImageBitmap(
                        BitmapFactory.decodeFile(
                            plantDisease.image
                        )
                    )
                }
                Log.i("ViewPlantDiseaseFragment", "Plant disease found: ${plantDisease.heading}")
            } else {
                // Set the text view to display that no plant disease was found
                binding.txtPlantDiseaseName.text = "No plant disease found"
                binding.deleteBtn.visibility = View.GONE
                Log.i("ViewPlantDiseaseFragment", "No plant disease found")
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
            Log.e("ViewPlantDiseaseFragment", "Error: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Function to show an alert dialog to confirm the deletion of the plant disease
    // If the user confirms the deletion, the plant disease is deleted from the database
    // The plant health status is updated to healthy if the plant was previously diseased
    private fun showAlertDialog(plant: Plant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Plant Disease")
            .setMessage("Are you sure you want to delete plant disease?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                try {
                    // On positive button click, delete the plant disease from the database
                    diseaseRepository = Repository(Disease::class)
                    diseaseRepository.deleteById(plantDisease._id)
                    Log.i(
                        "ViewPlantDiseaseFragment",
                        "Plant disease deleted: ${plantDisease.heading}"
                    )

                    // Update the UI to display that no plant disease was found
                    binding.txtPlantDiseaseName.text = getString(R.string.no_plant_disease_found)
                    binding.txtDescription.text = ""
                    binding.txtSymptoms.text = ""
                    binding.txtCauses.text = ""
                    binding.txtTreatment.text = ""
                    binding.deleteBtn.visibility = View.GONE

                    // Update the plant health status to healthy if the plant was previously diseased using a coroutine
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Find the latest plant in the database and update the health status to healthy
                            Database.realm.write {
                                val foundPlant = findLatest(plant)
                                if (foundPlant?.healthStatus == PlantHealthEnum.DISEASED.healthStats) {
                                    foundPlant.healthStatus = PlantHealthEnum.HEALTHY.healthStats
                                    Log.i(
                                        "ViewPlantDiseaseFragment",
                                        "Plant health status updated to healthy"
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "ViewPlantDiseaseFragment",
                                "Error updating plant health status: ${e.message}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ViewPlantDiseaseFragment", "Error deleting plant disease: ${e.message}")
                }
            }
            .show()
    }

}