package com.example.flourish

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.ActivityPlantBinding

// Activity for displaying the plant details and other fragments related to the plant
// The activity displays the plant details in the ViewPlantFragment
class PlantActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlantBinding
    private lateinit var pref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the plant activity layout
        binding = ActivityPlantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the ViewPlantFragment as the initial fragment to display the plant details
        val viewPlantFrag = ViewPlantFragment()

        // Get a plantId from the shared preferences
        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        pref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val plantId = pref.getString("plantId", null)

        // If the plant ID is null, display a toast message and return to the home activity
        if (plantId == null) {
            Log.i("PlantActivity", "Plant not found")
            Toast.makeText(this, "Plant not found", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this@PlantActivity, HomeActivity::class.java))
            finish()
        }
        // If the plant ID is not null, display the plant details in the ViewPlantFragment
        else {
            Log.i("PlantActivity", "Plant found: $plantId")
            // Set the plant ID in the main view model
            viewModel.plantId = plantId

            // Display the ViewPlantFragment in the fragment container
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container_view, viewPlantFrag)
                commit()
            }
        }
    }
}

// The MainViewModel class is used to store the plant ID and pass it between fragments
class MainViewModel : ViewModel() {
    var plantId = ""
}