package com.example.flourish

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flourish.adapter.AdapterPlant
import com.example.flourish.databinding.ActivityHomeBinding
import com.example.flourish.db.Repository
import com.example.flourish.model.Plant
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

// HomeActivity class is the home activity of the app
// It displays all the plants that the user has added to the app
// User can add a new plant, view the plant details, remove a plant, and logout from the app
class HomeActivity : AppCompatActivity(), AdapterPlant.RecyclerViewClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var plantList: ArrayList<Plant>
    private lateinit var adapterPlant: AdapterPlant
    private lateinit var plantRepository: Repository<Plant>
    private lateinit var sharedPreferences: SharedPreferences
    lateinit var text: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Bind layout to the activity
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the recycler view layout manager
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)

        // Get the Firebase authentication instance
        // Later data fetched from the firebase can be used to save the user's data in the app
        // and bind plants to a particular user
        auth = FirebaseAuth.getInstance()

        // Set the toolbar title and icon
        binding.materialToolbar.overflowIcon =
            AppCompatResources.getDrawable(this, R.drawable.more_vert_24)

        // Get the shared preferences instance
        // Set the plantId and searchType to null
        sharedPreferences = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("plantId", null).apply()
        sharedPreferences.edit().putString("searchType", null).apply()

        // Set the toolbar menu item click listener to navigate to the settings activity or logout
        binding.materialToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings -> {
                    startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
                }

                else -> {
                    auth.signOut()
                    startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                    finish()
                }
            }
            return@setOnMenuItemClickListener true
        }

        plantList = ArrayList()

        // Get the plants from the database
        plantRepository = Repository(Plant::class)
        plantList = ArrayList(plantRepository.getAll())

        // If no plants are found, display a toast message
        if (plantList.isEmpty()) {
            Toast.makeText(this, "No plants found", Toast.LENGTH_SHORT).show()
            Log.i("HomeActivity", "No plants found")
        }
        // Else display the plants in the recycler view using the adapter
        else {
            adapterPlant = AdapterPlant(plantList, this)
            binding.recyclerView.adapter = adapterPlant
            Log.i("HomeActivity", "Plants found and displayed")
        }

        // Set the add plant button click listener to navigate to the search activity
        binding.addPlant.setOnClickListener {
            sharedPreferences.edit().putString("searchType", "plant").apply()
            startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
            finish()
        }
    }

    // On item click listener to navigate to the plant activity to view the plant details for the selected plant
    override fun onItemClick(position: Int) {
        // Set the searchType to disease and plantId to the selected plant's id and pass it to the plant activity
        sharedPreferences.edit().putString("searchType", "disease").apply()
        val plant = plantList[position]
        sharedPreferences.edit().putString("plantId", plant._id.toHexString()).apply()
        startActivity(Intent(this@HomeActivity, PlantActivity::class.java))
        finish()
    }

    // On remove plant click listener to remove the selected plant from the database
    override fun onRemovePlantClick(position: Int) {
        // Get the selected plant
        val plant = plantList[position]
        // Show an alert dialog to confirm the deletion of the plant
        showAlertDialog(plant, position)
    }

    // Function to show an alert dialog to confirm the deletion of the plant
    private fun showAlertDialog(plant: Plant, position: Int) {
        // Show an alert dialog to confirm the deletion of the plant
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Plant")
            .setMessage("Are you sure you want to delete this plant?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                // Delete the plant from the database on positive button click
                plantRepository.deleteById(plant._id)
                plantList.removeAt(position)
                // Update the recycler view that the plant has been removed
                adapterPlant.notifyItemRemoved(position)
                // Dismiss the dialog
                dialog.dismiss()
            }
            .show()
    }
}
