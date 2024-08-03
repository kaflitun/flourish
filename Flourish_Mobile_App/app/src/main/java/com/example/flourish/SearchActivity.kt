package com.example.flourish

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flourish.databinding.ActivitySearchBinding

// Activity for searching for a plant or disease by photo or name
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var pref: SharedPreferences
    private lateinit var searchType: String
    private lateinit var searchModel: SearchModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to the search activity layout
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the SearchModel instance
        searchModel = ViewModelProvider(this)[SearchModel::class.java]

        // Get the search type and plant ID from the shared preferences
        pref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        searchType = pref.getString("searchType", null).toString()
        val plantId = pref.getString("plantId", null)

        // Set the plant ID in the search model if it is not null
        if (plantId != null) {
            searchModel.plantId = plantId
        }

        // Set the search type in the search model
        searchModel.searchType = searchType

        // Display the search fragment in the fragment container
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container_view, SearchFragment())
            commit()
        }
        // Return to the home activity when the back button is clicked
        binding.backBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            this.finish()
        }
    }
}

// The SearchModel class is used to store the search type and plant ID
// It is used to pass data between fragments
class SearchModel : ViewModel() {
    var searchType = ""
    var plantId = ""
}