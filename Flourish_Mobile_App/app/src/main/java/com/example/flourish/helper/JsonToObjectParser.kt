package com.example.flourish.helper

import android.util.Log
import com.example.flourish.model.Disease
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

// JsonToObjectParser class is a helper class to parse JSON object to object
// This class contains companion object which contains functions to parse JSON object to object
class JsonToObjectParser {
    companion object {
        // parsePlantParamsJsonToObject function is used to parse JSON object to HealthParameters object
        fun parsePlantParamsJsonToObject(json: JSONObject): HealthParameters {
            val healthParameters = HealthParameters()
            try {
                healthParameters.nitrogen =
                    BigDecimal(json.getString("nitrogen")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.phosphorus =
                    BigDecimal(json.getString("phosphorus")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.potassium =
                    BigDecimal(json.getString("potassium")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.ph =
                    BigDecimal(json.getString("ph")).setScale(2, RoundingMode.HALF_EVEN).toFloat()
                healthParameters.humidity =
                    BigDecimal(json.getString("humidity")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.temperature =
                    BigDecimal(json.getString("temperature")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.lightLevel =
                    BigDecimal(json.getString("lightLevel")).setScale(2, RoundingMode.HALF_EVEN)
                        .toFloat()
                healthParameters.dateTime = DateTimeHandler.getCurrentDateTimeFormatted()
            } catch (e: JSONException) {
                Log.e("JsonToObjectParser", "Error parsing JSON: ${e.message}")
            }
            return healthParameters
        }

        // parsePlantJsonToObject function is used to parse JSON object to Plant object
        fun parsePlantJsonToObject(json: JSONObject): Plant {
            val plant = Plant()
            try {
                plant.name = json.getString("plant_name")
                plant.description = json.getString("description")
                plant.requiredWatering = json.getString("watering")
                plant.requiredLight = json.getString("light")
                plant.requiredSoilParameters = json.getString("soil")
                plant.requiredFeeding = json.getString("feeding")
            } catch (e: JSONException) {
                Log.e("JsonToObjectParser", "Error parsing JSON: ${e.message}")
            }
            return plant
        }

        // parsePlantDiseaseJsonToObject function is used to parse JSON object to Disease object
        fun parsePlantDiseaseJsonToObject(json: JSONObject): Disease {
            val disease = Disease()
            try {
                disease.heading = json.getString("plant_disease_name")
                disease.description = json.getString("description")
                disease.symptoms = json.getString("symptoms")
                disease.treatment = json.getString("treatment")
                disease.causes = json.getString("causes")

            } catch (e: JSONException) {
                Log.e("JsonToObjectParser", "Error parsing JSON: ${e.message}")
            }
            return disease
        }
    }
}