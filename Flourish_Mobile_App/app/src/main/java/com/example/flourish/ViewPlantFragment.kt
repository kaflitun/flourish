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
import com.example.flourish.databinding.FragmentViewPlantBinding
import com.example.flourish.db.Repository
import com.example.flourish.model.Plant
import org.mongodb.kbson.ObjectId

// Fragment to view the plant details for as selected plant from the home activity
// The plant details are displayed in the text views and image view
class ViewPlantFragment : Fragment() {

    private var _binding: FragmentViewPlantBinding? = null
    private val binding: FragmentViewPlantBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plant: Plant
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository : Repository<Plant>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Bind the view and return it to the fragment manager to display it on the screen
        _binding = FragmentViewPlantBinding.inflate(inflater, container, false)
        plantRepository = Repository(Plant::class)
        return _binding?.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the main view model from the activity
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the plant id from the main view model and convert it to an ObjectId
        plantId = ObjectId(mainViewModel.plantId)

        // Set the on click listener for the back button to remove the current fragment and go back to the home activity
        binding.backBtn.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                remove(this@ViewPlantFragment)
                commit()
            }
            startActivity(Intent(requireActivity(), HomeActivity::class.java))
            requireActivity().finish()
        }

        // Set the on click listener for the manage plant button to navigate to the manage plant fragment
        binding.managePlant.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }
        try {
            // Get the plant from the database using the plant id
            plant = plantRepository.getById(plantId)!!
            Log.i("ViewPlantFragment", "Plant found: ${plant.name}")

            // Set the plant details to the text views and image view if the plant is not null
            plant.let {
                binding.plantName.text = plant.name
                binding.healthStatus.text = "Health Status: " + plant.healthStatus
                binding.description.text = plant.description
                binding.watering.text = "Watering: " + plant.requiredWatering
                binding.feeding.text = "Feeding: " + plant.requiredFeeding
                binding.soilParams.text = "Soil Parameters: " + plant.requiredFeeding
                binding.light.text = "Light: " + plant.requiredLight
                if(plant.image.isNotEmpty()){
                    // Set the plant image to the image view
                    binding.plantImage.setImageBitmap(BitmapFactory.decodeFile(plant.image))
                }
            }
        }
        catch (e: Exception){
            Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
            Log.e("ViewPlantFragment", e.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}