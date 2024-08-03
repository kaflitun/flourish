package com.example.flourish

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentWateringScheduleBinding
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.model.Plant
import com.example.flourish.model.WateringSchedule
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.text.SimpleDateFormat
import java.util.Calendar


// Fragment for plant watering schedule management
// This fragment allows the user to set the watering schedule for a plant
// The user can set the time and days of the week when the plant should be watered
// The user can also remove the watering schedule
class WateringScheduleFragment : Fragment() {
    private var _binding: FragmentWateringScheduleBinding? = null
    private val binding: FragmentWateringScheduleBinding get() = _binding!!
    private lateinit var mainViewModel: MainViewModel
    private lateinit var plant: Plant
    private lateinit var plantId: ObjectId
    private lateinit var plantRepository: Repository<Plant>
    private var updWateringSchedule: WateringSchedule = WateringSchedule()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        plantRepository = Repository(Plant::class)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bidding the layout to the fragment class and returning the root view of the layout
        _binding = FragmentWateringScheduleBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the plantId from the MainViewModel class and convert it to ObjectId
        plantId = ObjectId(mainViewModel.plantId)

        try {
            // Get the plant from the database by the plantId
            plant = plantRepository.getById(plantId)!!

            // Check if the plant has a watering schedule set
            if (plant.wateringSchedule != null) {
                Log.i("WateringScheduleFragment", "Plant found: ${plant.name}")
                // Set the time and days of the week for the watering schedule in the UI
                binding.timeTxt.text = plant.wateringSchedule!!.time
                val weekDays = plant.wateringSchedule!!.weekDays
                // Check the days of the week that are set in the watering schedule
                if (weekDays.isNotEmpty()) {
                    for (day in weekDays) {
                        when (day) {
                            "Monday" -> binding.chkMonday.isChecked = true
                            "Tuesday" -> binding.chkTuesday.isChecked = true
                            "Wednesday" -> binding.chkFriday.isChecked = true
                            "Thursday" -> binding.chkThursday.isChecked = true
                            "Friday" -> binding.chkFriday.isChecked = true
                            "Saturday" -> binding.chkSaturday.isChecked = true
                            "Sunday" -> binding.chkSunday.isChecked = true
                        }
                    }
                }
            } else {
                // Hide the time text view if the watering schedule is not set
                binding.timeTxt.visibility = View.GONE
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error occurred", Toast.LENGTH_SHORT).show()
            Log.e("WateringScheduleFragment", e.toString())
        }
        // Clear the watering schedule set in the UI when the clear button is clicked
        binding.clearBtn.setOnClickListener {
            binding.timeTxt.visibility = View.GONE
            binding.chkMonday.isChecked = false
            binding.chkTuesday.isChecked = false
            binding.chkWednesday.isChecked = false
            binding.chkThursday.isChecked = false
            binding.chkFriday.isChecked = false
            binding.chkSaturday.isChecked = false
            binding.chkSunday.isChecked = false

        }
        // Update the watering schedule in the database when the update button is clicked
        binding.updateBtn.setOnClickListener {
            // Check if the time and at least one day of the week is set
            if (binding.timeTxt.visibility == View.GONE
                && (!binding.chkMonday.isChecked && !binding.chkTuesday.isChecked &&
                        !binding.chkWednesday.isChecked && !binding.chkThursday.isChecked &&
                        !binding.chkFriday.isChecked && !binding.chkSaturday.isChecked &&
                        !binding.chkSunday.isChecked)
            ) {
                // Remove the watering schedule from the database using coroutines and realm transactions
                CoroutineScope(Dispatchers.Main).launch {
                    // Write the changes to the database in the IO thread
                    withContext(Dispatchers.IO) {
                        Database.realm.write {
                            // Find the latest plant object in the database
                            val managedPlant = findLatest(plant)
                            // Remove the watering schedule from the plant object
                            managedPlant?.wateringSchedule = null
                        }
                    }
                }
                Toast.makeText(requireContext(), "Schedule has been removed", Toast.LENGTH_SHORT)
                    .show()
                Log.i("WateringScheduleFragment", "Schedule has been removed")
            }
            // Check if the time or days of the week is not set and show a toast message
            else if (binding.timeTxt.visibility == View.GONE
                || (!binding.chkMonday.isChecked && !binding.chkTuesday.isChecked &&
                        !binding.chkWednesday.isChecked && !binding.chkThursday.isChecked &&
                        !binding.chkFriday.isChecked && !binding.chkSaturday.isChecked &&
                        !binding.chkSunday.isChecked)
            ) {
                Toast.makeText(
                    requireContext(),
                    "Please select the time and at least one day",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Update the watering schedule in the database using coroutines and realm transactions
            else {
                try {
                    // Add only days of the week that are checked to the weekDays list
                    val weekDays: RealmList<String> = realmListOf()

                    if (binding.chkMonday.isChecked) weekDays.add("Monday")
                    if (binding.chkTuesday.isChecked) weekDays.add("Tuesday")
                    if (binding.chkWednesday.isChecked) weekDays.add("Wednesday")
                    if (binding.chkThursday.isChecked) weekDays.add("Thursday")
                    if (binding.chkFriday.isChecked) weekDays.add("Friday")
                    if (binding.chkSaturday.isChecked) weekDays.add("Saturday")
                    if (binding.chkSunday.isChecked) weekDays.add("Sunday")

                    updWateringSchedule.weekDays = weekDays
                    updWateringSchedule.time = binding.timeTxt.text.toString()

                    // Write the changes to the database in the IO thread using coroutines and realm transactions
                    CoroutineScope(Dispatchers.Main).launch {
                        // Write the changes to the database in the IO thread
                        withContext(Dispatchers.IO) {
                            Database.realm.write {
                                val managedPlant = findLatest(plant)
                                managedPlant?.wateringSchedule = updWateringSchedule
                            }
                        }
                    }
                    Toast.makeText(
                        requireContext(),
                        "Schedule has been updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i("WateringScheduleFragment", "Schedule has been updated")
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error occurred. Schedule is not updated",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("WateringScheduleFragment", "Error saving data to database: ${e.message}")
                }
            }
        }

        binding.btnBack.setOnClickListener {
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

                // Show time in the time text view in the UI
                binding.timeTxt.visibility = View.VISIBLE
                binding.timeTxt.text = SimpleDateFormat("HH:mm").format(cal.time)
            }
            // Show the time picker dialog in the UI
            TimePickerDialog(
                requireContext(),
                timeSetListener,
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Set the binding to null when the view is destroyed to avoid memory leaks
        _binding = null
    }
}