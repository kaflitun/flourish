package com.example.flourish

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentFertilisingScheduleBinding
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.helper.DateTimeHandler
import com.example.flourish.model.FertilisingSchedule
import com.example.flourish.model.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.Calendar

// The fragment allows a user to set the fertilising schedule for a plant
// User can set the date and time for the fertilising schedule of the plant
// User can also remove the fertilising schedule for the plant
class FertilisingScheduleFragment : Fragment() {
    private var _binding: FragmentFertilisingScheduleBinding? = null
    private val binding: FragmentFertilisingScheduleBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plant: Plant
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository : Repository<Plant>
    private var updFertilisingSchedule: FertilisingSchedule = FertilisingSchedule()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get view model and repository instances
        plantRepository = Repository(Plant::class)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind layout to the fragment
        _binding = FragmentFertilisingScheduleBinding.inflate(inflater, container, false)
        return _binding?.root
    }
    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get plantId from the view model
        plantId = ObjectId(mainViewModel.plantId)

        try {
            // Get plant details from the database using the plantId
            plant = plantRepository.getById(plantId)!!
            if(plant.fertilisingSchedule != null) {
                // Set the date and time for the fertilising schedule of the plant if it exists
                binding.timeTxt.text = plant.fertilisingSchedule!!.time
                binding.dateTxt.text = plant.fertilisingSchedule!!.date
            }
            else{
                // Hide the date and time text views if the fertilising schedule does not exist
                binding.timeTxt.visibility = View.GONE
                binding.dateTxt.visibility = View.GONE
            }
        }
        catch (e: Exception) {
            Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
            Log.e("FertilisingScheduleFragment", e.toString())
        }

        // Clear the date and time text views when the clear button is clicked
        binding.clearBtn.setOnClickListener {
            binding.timeTxt.visibility = View.GONE
            binding.dateTxt.visibility = View.GONE
        }

        // Update the fertilising schedule for the plant when the update button is clicked
        binding.updateBtn.setOnClickListener{
            // If the date and time text views are hidden, remove the fertilising schedule
            if(binding.timeTxt.visibility == View.GONE && binding.dateTxt.visibility == View.GONE){
                // Remove the fertilising schedule from the plant using coroutines and realm
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        Database.realm.write {
                            // Find the latest plant and set the fertilising schedule to null
                            val managedPlant = findLatest(plant)
                            managedPlant?.fertilisingSchedule = null
                        }
                    }
                }
                Toast.makeText(requireContext(), "Schedule has been removed", Toast.LENGTH_SHORT).show()
                Log.i("FertilisingScheduleFragment", "Schedule has been removed")
            }
            // If the date or time text views are hidden, display a toast message
            else if(binding.timeTxt.visibility == View.GONE || binding.dateTxt.visibility == View.GONE){
                Toast.makeText(requireContext(), "Please add both date and time", Toast.LENGTH_SHORT).show()
            }
            // Else update the fertilising schedule for the plant
            else{
                CoroutineScope(Dispatchers.Main).launch {
                    // Update the fertilising schedule for the plant using coroutines and realm
                    withContext(Dispatchers.IO) {
                        Database.realm.write {
                            // Find the latest plant and update the fertilising schedule
                            val managedPlant = findLatest(plant)
                            // Set the time and date for the fertilising schedule
                            updFertilisingSchedule.time = binding.timeTxt.text.toString()
                            updFertilisingSchedule.date = DateTimeHandler.getDateFormatted(binding.dateTxt.text.toString())
                            // Set the fertilising schedule for the plant
                            managedPlant?.fertilisingSchedule = updFertilisingSchedule
                        }
                    }
                }
                Toast.makeText(requireContext(), "Schedule has been updated", Toast.LENGTH_SHORT).show()
                Log.i("FertilisingScheduleFragment", "Schedule has been updated")
            }
        }
        // Navigate back to the manage plant fragment when the back button is clicked
        binding.btnBack.setOnClickListener{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }

        //Set the time picker dialog for the time button
        binding.timeBtn.setOnClickListener {
            val cal = Calendar.getInstance()
            // Set the time picker dialog for the time button
            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                binding.timeTxt.visibility = View.VISIBLE
                binding.timeTxt.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            // Show the time picker dialog for the time button
            TimePickerDialog(requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(
                Calendar.MINUTE), true).show()
        }
        //Set the date picker dialog for the date button
        binding.dateBtn.setOnClickListener{
            val cal = Calendar.getInstance()
            // Set the date picker dialog for the date button
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.dateTxt.visibility = View.VISIBLE
                binding.dateTxt.text = SimpleDateFormat("dd/MM/yyyy").format(cal.time)
            }
            // Show the date picker dialog for the date button
            DatePickerDialog(requireContext(), dateSetListener, cal.get(Calendar.YEAR), cal.get(
                Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Unbind the view when the fragment is destroyed to avoid memory leaks
        _binding = null
    }
}