package com.example.flourish.enum

// PlantHealthEnum is an enum class to store the health statuses of a plant
// It is used to keep all possible health statuses of a plant in one place
enum class PlantHealthEnum(val healthStats: String)  {
    HEALTHY("Healthy"), //No signs of stress or disease
    WATER_DEFICIENT("Water deficient"), // Needs watering
    LIGHT_DEFICIENT("Light deficient"), // Needs light
    NUTRIENT_DEFICIENT("Nutrients deficient"), // Needs nutrients
    TEMPERATURE_DEFICIENT("Temperature deficient"), // Needs higher temperature
    DISEASED("Diseased") //Signs of disease
}