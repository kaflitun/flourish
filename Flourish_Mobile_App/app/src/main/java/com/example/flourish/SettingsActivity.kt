package com.example.flourish

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flourish.databinding.ActivitySettingsBinding

// SettingsActivity class is used to display the settings page
// Currently, the settings page is empty and not functional but can be used to add settings in the future
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener{
            startActivity(Intent(this@SettingsActivity, HomeActivity::class.java))
            finish()
        }
    }
}