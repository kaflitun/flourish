package com.example.flourish.helper

import com.example.flourish.model.Plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

// GptApiRequestHandler class is a helper class used to send requests to OpenAI API using coroutines
// and get the response back
// It also creates the JSON request for different types of requests
// All JSON requests are created based on the OpenAI API documentation for GPT-3.5 and GPT-4 models
// Requests are using natural language processing to get the required information
class GptApiRequestHandler {
    companion object {

        // sendRequestToOpenAIWithCoroutines is a main function to send a request to OpenAI API
        fun sendRequestToOpenAIWithCoroutines(requestJSON: String, callback: (String) -> Unit) {
            // CoroutineScope is used to launch a new coroutine on the IO thread
            CoroutineScope(Dispatchers.IO).launch {
                // OkHttpClient is used to create a new HTTP client with a timeout of 30 seconds for read, write, and connect
                val client = OkHttpClient().newBuilder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                // Create a media type for JSON content
                val mediaType = "application/json; charset=utf-8".toMediaType()

                // Create a request body with the request JSON
                val body = requestJSON.toRequestBody(mediaType)

                // Create a request with the URL, POST method, headers, and body
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader(
                        "Authorization",
                        "Bearer api-token"
                    )
                    .build()

                // Execute the request and get the response back
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        callback(response.message)
                    } else {
                        callback(response.body!!.string())
                    }
                }
            }
        }

        // createRequestPlantImage function is used to create a JSON request
        // for plant image identification based on the plant image
        fun createRequestPlantImage(plantImg: String): String {
            return """
            {
                "model": "gpt-4-vision-preview",
                "messages": [
                    {
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": "Identify the plant on the photo and return only the plant name as a string. If it is not a plant return: This is not a plant. If it is difficult to identify the plant, try to assume what it might be and return only a string name. If it is impossible to identify the plant, return: Unknown plant."
                                
                            },
                            {
                                "type": "image_url", 
                                "image_url": {
                                    "url": "data:image/jpeg;base64,$plantImg"
                                }
                            }
                        ]
                    }
                ],
                "max_tokens": 50
            }
        """.trimIndent()
        }

        // createRequestPlantDiseaseImage function is used to create a JSON request
        // for plant disease image identification based on the plant disease image
        fun createRequestPlantDiseaseImage(plantDiseaseImg: String): String {
            return """
            {
                "model": "gpt-4-vision-preview",
                "messages": [
                    {
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": "Identify a plant disease or plant problem (include overwatering,sunburn, aphids and other ) on the photo and return only the plant disease name or plant plant problem name as a string. If there is no a plant disease or plant problem return: No plant disease found. If it is difficult to identify the plant disease or plant problem, try to assume what it might be and return only a string name. If it is impossible to identify the plant disease or plant problem, return: Unknown plant disease."
                                
                            },
                            {
                                "type": "image_url", 
                                "image_url": {
                                    "url": "data:image/jpeg;base64,$plantDiseaseImg"
                                }
                            }
                        ]
                    }
                ],
                "max_tokens": 50
            } 
        """.trimIndent()
        }

        // createRequestPlantDescription function is used to create a JSON request
        // for plant description based on the plant name
        fun createRequestPlantDescription(plantName: String): String {
            return """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [
                    {
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": "Given a plant name query, return a structured JSON response less than 250 tokens with detailed information. If the $plantName is not a plant name, the response should indicate this with a JSON object stating plant: false. If the input name corresponds to a real existing plant, provide detailed care instructions and descriptions for the plant '$plantName', returning a JSON with the following structure: plant: true, plant_name: change $plantName name to be a correct plant name and first letter is uppercase (use this new name everywhere in the text), description: Provide a 50-word description of $plantName, watering: Describe how often $plantName needs watering,feeding: Describe when $plantName should be fed, soil: Describe what kind of soil is required for $plantName,light: State whether $plantName needs direct or indirect sunlight."
                                
                            }
                        ]
                    }
                ],
                "max_tokens": 260,
                "response_format": { "type": "json_object" }
            }
        """.trimIndent()
        }

        // createRequestPlantParams function is used to create a JSON request
        // for required plant health parameters based on the plant name
        fun createRequestPlantParams(plantName: String): String {
            return """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [
                    {
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": "Given a plant health parameters query, return a structured JSON response less than 200 tokens with detailed information. Provide plant health parameters for the plant '$plantName', returning a JSON with the following structure: temperature: Required temperature for the $plantName plant return only a number in celsius, ph: Required pH for the $plantName plant return only a number, nitrogen: Required nitrogen for the $plantName plant return only a number in mg/kg, potassium: Required potassium for the $plantName plant return only a number in mg/kg, phosphorus: Required phosphorus for the $plantName plant return only a number in mg/kg, humidity: Required humidity for the $plantName plant return only a number in %, lightLevel: Required light level for the $plantName plant return only a number in lux."
                                
                            }
                        ]
                    }
                ],
                "max_tokens": 200,
                "response_format": { "type": "json_object" }
            }
        """.trimIndent()
        }

        // createRequestPlantDiseaseDescription function is used to create a JSON request
        // for plant disease description based on the plant disease name
        fun createRequestPlantDiseaseDescription(plantDiseaseName: String): String {
            return """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [
                    {
                        "role": "user", 
                        "content": [
                            {
                                "type": "text",
                                "text": "Given a plant disease name query, return a structured JSON response less than 445 tokens with detailed information. If the $plantDiseaseName is not a plant disease name, the response should indicate this with a JSON object stating plant_disease: false. If the input name corresponds to a real existing plant disease, provide detailed care instructions and descriptions for the plant disease '$plantDiseaseName', returning a JSON with the following structure: plant_disease: true, plant_disease_name: real name of plant disease (use this new real name everywhere in the text instead of provided one), description: Provide a 50-word description of $plantDiseaseName plant disease, treatment: Describe how to treat $plantDiseaseName plant disease, symptoms: Describe the symptoms of $plantDiseaseName plant disease, causes: Describe the causes of $plantDiseaseName plant disease."
                                
                            }
                        ]
                    }
                ],
                "max_tokens": 450,
                "response_format": { "type": "json_object" }
            }
        """.trimIndent()
        }

        // createRequestPlantCareRecommendation function is used to create a JSON request
        // for plant care recommendation based on the plant information
        fun createRequestPlantCareRecommendation(plant: Plant): String {
            val plantName : String = plant.name
            val plantWatering: String = if (plant.wateringSchedule == null) "" else "current watering schedule in a format of days of the week: " +
                    "" + plant.wateringSchedule!!.weekDays + ", "
            val plantFeeding : String = if (plant.fertilisingSchedule == null) "" else "current fertilising schedule in a format of date: " +
                    "" + plant.fertilisingSchedule!!.date + " " + plant.fertilisingSchedule!!.time  + ", "
            var healthParameters = ""

            if(plant.healthParameters.lastOrNull() != null){
                val healthParams = plant.healthParameters.last()
                if (healthParams.temperature != 0.0F && healthParams.ph != 0.0F &&
                    healthParams.nitrogen != 0.0F && healthParams.potassium != 0.0F &&
                    healthParams.phosphorus != 0.0F && healthParams.humidity != 0.0F &&
                    healthParams.lightLevel != 0.0F ) {
                    healthParameters = "current health parameters: temperature: " + healthParams.temperature + "°C, ph: " + healthParams.ph + "" +
                            ", nitrogen: " + healthParams.nitrogen + "mg/kg, potassium: " + healthParams.potassium + "mg/kg, " +
                            "phosphorus: " + healthParams.phosphorus + "mg/kg, humidity: " + healthParams.humidity + "%, " +
                            "light level: " + healthParams.lightLevel + "lux" + ", "
                }
            }

            val plantDisease : String = if(plant.disease == null) "" else "current plant diseases: " + plant.disease!!.heading + ", "

            return """
            {
                "model": "gpt-3.5-turbo-1106",
                "messages": [
                    {
                        "role": "user", 
                        "content": [ 
                            {
                                "type": "text",
                                "text": "Given a plant $plantName along with current plant care information: $plantWatering $plantFeeding $healthParameters $plantDisease; return a detailed plant care recommendation divided into paragraphs based on the provided information no more than 450 tokens you also may include watering/fertilising schedule adjustments if required. If plant name '$plantName' is not a plant name, return : 'No information found'."
                                
                            }
                        ]
                    }
                ],
                "max_tokens": 460
            }
        """.trimIndent()
        }
    }
}