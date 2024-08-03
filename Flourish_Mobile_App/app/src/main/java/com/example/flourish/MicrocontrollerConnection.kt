package com.example.flourish

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.FragmentMicrocontrollerConnectionBinding
import com.example.flourish.db.Repository
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import com.example.flourish.sensorDataHandlingService.SensorDataService
import com.example.flourish.sensorDataHandlingService.SensorDataService.Companion.ACTION_UPDATE_SENSOR_DATA
import com.example.flourish.sensorDataHandlingService.SensorDataService.Companion.EXTRA_SENSOR_DATA
import com.google.gson.Gson

// Fragment is used to connect to the microcontroller and display live sensor data
// It uses a broadcast receiver to receive sensor data from the SensorDataService and display it on the UI
// User can start and stop the foreground service using the buttons provided
class MicrocontrollerConnection : Fragment() {

    private var _binding: FragmentMicrocontrollerConnectionBinding? = null
    private val binding: FragmentMicrocontrollerConnectionBinding get() = _binding!!
    private lateinit var plantId: String
    private lateinit var plantRepository: Repository<Plant>
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sensorDataReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set mainViewModel and plantRepository instances
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        plantRepository = Repository(Plant::class)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Bind the layout for this fragment
        _binding = FragmentMicrocontrollerConnectionBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the plantId from the mainViewModel
        plantId = mainViewModel.plantId
        // Create a broadcast receiver to receive sensor data
        createBroadcastReceiver()
        // Set the text view to display waiting for updates
        binding.updatesTxt.text = getString(R.string.waiting_for_updates)

        // Set the back button to navigate to the ManagePlantFragment
        binding.btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.fragment_container_view, ManagePlantFragment())
                commit()
            }
        }

        // Set the connect and disconnect buttons to start and stop the foreground service
        binding.connectBtn.setOnClickListener {
            // Start the foreground service to connect to the microcontroller
            Intent(context, SensorDataService::class.java).also { serviceIntent ->
                // Pass the plantId to the service
                serviceIntent.putExtra("plantId", plantId)
                context?.startForegroundService(serviceIntent)
            }
        }

        // Stop the foreground service when the disconnect button is pressed
        binding.disconnectBtn.setOnClickListener {
            // Stop the foreground service to disconnect from the microcontroller
            Intent(context, SensorDataService::class.java).also { serviceIntent ->
                context?.stopService(serviceIntent)
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    // Create a broadcast receiver to receive sensor data from the SensorDataService and update the UI
    private fun createBroadcastReceiver() {
        // Set up the broadcast receiver
        sensorDataReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            // Override the onReceive method to receive sensor data and update the UI
            override fun onReceive(context: Context, intent: Intent) {
                // Check if the intent action is ACTION_UPDATE_SENSOR_DATA
                if (intent.action == ACTION_UPDATE_SENSOR_DATA) {
                    // Get sensor data from the intent
                    val healthParamsJson = intent.getStringExtra(EXTRA_SENSOR_DATA)
                    // Check if the sensor data is not null
                    if (healthParamsJson != null) {
                        try {
                            // Convert the sensor data from JSON to HealthParameters object using Gson
                            val healthParams: HealthParameters =
                                Gson().fromJson(healthParamsJson, HealthParameters::class.java)
                            // Update UI with the latest sensor data
                            binding.updatesTxt.text = "Latest Params: \n" +
                                    "\nDate : ${healthParams.dateTime}\n" +
                                    "\nNitrogen : ${healthParams.nitrogen}\n" +
                                    "Phosphorus : ${healthParams.phosphorus}\n" +
                                    "Potassium : ${healthParams.potassium}\n" +
                                    "PH : ${healthParams.ph}\n" +
                                    "Humidity : ${healthParams.humidity}\n" +
                                    "Temperature : ${healthParams.temperature}\n" +
                                    "Light level : ${healthParams.lightLevel}\n"
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error retrieving data", Toast.LENGTH_SHORT)
                                .show()
                            Log.e(
                                "MicrocontrollerConnection",
                                "Error retrieving data: ${e.message}"
                            )
                        }
                    }
                }
            }
        }
        // Register the broadcast receiver with the intent filter ACTION_UPDATE_SENSOR_DATA to receive sensor data updates
        val filter = IntentFilter(ACTION_UPDATE_SENSOR_DATA)
        requireActivity().registerReceiver(sensorDataReceiver, filter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister the broadcast receiver when the fragment is destroyed
        requireActivity().unregisterReceiver(sensorDataReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Set the binding to null when the fragment is destroyed to avoid memory leaks
        _binding = null
    }
}