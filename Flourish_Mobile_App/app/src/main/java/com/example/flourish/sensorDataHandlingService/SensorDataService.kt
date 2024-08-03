package com.example.flourish.sensorDataHandlingService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.flourish.R
import com.example.flourish.db.Database
import com.example.flourish.db.Repository
import com.example.flourish.helper.JsonToObjectParser
import com.example.flourish.helper.PlantHealthStatusManager
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.mongodb.kbson.BsonObjectId
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// Foreground service to fetch data from the sensor every minute
class SensorDataService : Service() {

    // Create a Coroutine Scope for IO-bound operations
    // Coroutine Scope is used to launch coroutines in the IO context
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Notification channel ID and notification ID for the foreground service
    private val notificationChannelID = "sensor_data_service_channel"
    private val notificationID = 1
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var plantId: String? = null
    private var startTime: LocalDateTime = LocalDateTime.now()
    private var isServiceInitialized = false
    private var isServiceStopped = false
    private val healthParamsList: MutableList<HealthParameters> = mutableListOf()

    companion object {
        // Action to update the sensor data in the UI using a broadcast receiver
        const val ACTION_UPDATE_SENSOR_DATA = "com.example.flourish.UPDATE_SENSOR_DATA"

        // Extra to store the sensor data in the broadcast intent
        const val EXTRA_SENSOR_DATA = "com.example.flourish.EXTRA_SENSOR_DATA"
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve plantId from the Intent that started the service
        plantId = intent?.getStringExtra("plantId")

        if (plantId == null) {
            Log.i("SensorDataService", "No plantId provided")
        } else {
            Log.i("SensorDataService", "PlantId provided: $plantId")
        }

        // Start the foreground service if it is not already running
        // Used for the first time the service is started
        if (!isServiceInitialized) {
            startForeground(notificationID, createNotification())
            isServiceInitialized = true
        }
        return START_STICKY
    }

    // Create the service and start the foreground service
    // When the service is created, create a notification channel and start the foreground service
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        startForeground(notificationID, createNotification())

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            // Fetch data from the sensor
            fetchDataFromSensor()
            // Check if the data should be stored in the database
            checkAndStoreData()
            // Schedule the next run after each execution
            scheduleNextFetch()
        }

        // Ensuring we do not stack multiple fetches if service is recreated without being destroyed properly
        handler.post(runnable)
    }

    // Schedule the next data fetch after 1 minute using a handler and runnable
    private fun scheduleNextFetch() {
        if (!isServiceStopped) {
            handler.postDelayed(runnable, TimeUnit.MINUTES.toMillis(1))
        }
    }

    // Fetch data from the sensor every minute using OkHttp client
    private fun fetchDataFromSensor() {
        // Create a new coroutine to fetch data from the sensor
        serviceScope.launch {
            val client = OkHttpClient()
            // Create a request to fetch data from the sensor using the static IP address
            // Normally broadcast group address would be used to connect to a microcontroller and fetch data from the sensor
            // The IP address is hardcoded for ease and demonstration purposes
            val request = Request.Builder()
                .url("http://192.XXX.X.XXX")
                .build()

            try {
                // Execute the request and handle the response
                client.newCall(request).execute().use { response ->
                    // If the response is not successful, throw an exception
                    if (!response.isSuccessful) throw Exception("Failed to fetch data")
                    // Get the JSON data from the response body or throw an exception if no data is received
                    val jsonData = response.body?.string() ?: throw Exception("No data received")
                    // Parse the JSON data into a JSONObject
                    val jsonObject = JSONObject(jsonData)
                    // Store the data in the list
                    storeData(jsonObject)
                }
            } catch (e: Exception) {
                Log.e("SensorDataService", "Error fetching sensor data: ${e.message}")
                isServiceStopped = true
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    // Store the data received from the sensor in the list
    private fun storeData(data: JSONObject) {
        Log.i("SensorDataService", "Data received: $data")
        val healthParams: HealthParameters = JsonToObjectParser.parsePlantParamsJsonToObject(data)
        healthParamsList.add(healthParams)
        // Send a broadcast with the sensor data to update the UI
        sendBroadcast(healthParams)
    }

    // Check if the data should be stored in the database
    // If the time difference between the start time and the current time
    // Is greater than or equal to 60 minutes (Time can be reduced for demonstration purposes), store the data in the database
    private fun checkAndStoreData() {
        if (ChronoUnit.MINUTES.between(startTime, LocalDateTime.now()) >= 60) {
            serviceScope.launch {
                // Try to fetch the plant using the Repository
                val plantRepo = Repository(Plant::class)
                val plant: Plant? = plantId?.let { plantRepo.getById(BsonObjectId(it)) }
                // If the plant is not null, store the data in the database
                if (plant != null) {
                    try {
                        Database.realm.write {
                            val managedPlant = findLatest(plant)
                            managedPlant?.healthStatus =
                                PlantHealthStatusManager.setPlantHealthStatus(
                                    managedPlant!!,
                                    healthParamsList
                                )
                            managedPlant.healthParameters.add(healthParamsList.last())
                        }
                    } catch (e: Exception) {
                        Log.e("SensorDataService", "Error saving data to database: ${e.message}")
                    }
                    Log.i("SensorDataService", "Data saved to database for plant with ID: $plantId")
                    // Clear the list and reset the timer
                    healthParamsList.clear()
                    startTime = LocalDateTime.now()
                } else {
                    Log.e("SensorDataService", "Failed to find plant with ID: $plantId")
                }
            }
        }
    }

    // Create a notification for the foreground service
    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this, notificationChannelID)
            .setContentTitle("Sensor Data Fetching")
            .setContentText("Service is running in the background.")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return builder.build()
    }

    // Create a notification channel for the foreground service notification to be displayed
    private fun createNotificationChannel() {
        val name = "Sensor Data Service"
        val descriptionText = "This channel is used by sensor data service"
        // Set the importance to default to allow the notification to be displayed
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // Create the notification channel
        val channel = NotificationChannel(notificationChannelID, name, importance).apply {
            description = descriptionText
        }
        // Register the notification channel with the system to allow the notification to be displayed
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    // Send a broadcast with the sensor data to update the UI
    private fun sendBroadcast(healthParams: HealthParameters) {
        // Create an intent with the action to update the sensor data
        val intent = Intent(ACTION_UPDATE_SENSOR_DATA)
        // Convert the sensor data to JSON and add it as an extra to the intent
        val healthParamsJson = Gson().toJson(healthParams)
        intent.putExtra(EXTRA_SENSOR_DATA, healthParamsJson)
        sendBroadcast(intent)
    }

    // The service is not bound to any activity so return null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Cancel all coroutines and remove any pending callbacks when the service is destroyed
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when the service is destroyed
        serviceScope.cancel()
        // Remove any pending callbacks to prevent memory leaks
        handler.removeCallbacks(runnable)
    }
}