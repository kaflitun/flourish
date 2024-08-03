@file:Suppress("DEPRECATION")

package com.example.flourish

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn.*
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

// Main activity for the Flourish app
// Handles the Google Sign-In authentication and starts the HomeActivity if successful
class MainActivity : AppCompatActivity(){

    private lateinit var auth : FirebaseAuth
    private lateinit var googleSignInClient : GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In to request the user's ID token and email address
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Create a GoogleSignInClient object with the GoogleSignInOptions object
        googleSignInClient = getClient(this,gso)

        // Set click listener for the sign-in button
        findViewById<MaterialButton>(R.id.sign_in_button).setOnClickListener{
            signInGoogle()
        }
    }

    // Prompts the user to select a Google account and consent to the requested permissions
    private fun signInGoogle(){
        // Start the activity for Google Sign-In
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // Handles the result of the activity started for Google Sign-In
    // If result is successful, it extracts information about the signed-in Google account
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
            if(result.resultCode == Activity.RESULT_OK){
                // Extract the signed-in account
                val task = getSignedInAccountFromIntent(result.data)
                handleResult(task)
            }
    }

    // Checks if the Google Sign-In was successful
    // If successful, it extracts the signed-in account and updates the UI
    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            // Extract the signed-in account
            val account : GoogleSignInAccount? = task.result
            // Update the UI with the signed-in account
            if(account != null){
                updateUI(account)
            }
        }
        else{
            // Display error message if Google Sign-In fails
            Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
            Log.i("MainActivity", "Error: ${task.exception.toString()}")
        }
    }

    // Handles the Firebase authentication using the Google Sign-In credentials
    // If successful, it starts the HomeActivity and passes along the user information as extras
    private fun updateUI(account: GoogleSignInAccount) {
        // Firebase authentication using Google Sign-In credentials (ID token)
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        // Sign in with credential and handle the result
        auth.signInWithCredential(credential).addOnCompleteListener{
            if(it.isSuccessful){
                // Start HomeActivity and pass along the user information as extras
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("email", account.email)
                intent.putExtra("name", account.displayName)
                startActivity(intent)
            }
            else{
                // Display error message if authentication fails
                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                Log.i("MainActivity", "Error: ${it.exception.toString()}")
            }
        }
    }
}