package com.example.flourish.helper

import com.example.flourish.enum.PlantHealthEnum
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant

// PlantHealthStatusManager class is a helper class to manage the health status of a plant
// when the foreground service is running
// It sets the health status of a plant based on the health parameters and disease of the plant
// It uses the PlantHealthEnum class to set the health status of a plant
class PlantHealthStatusManager {
    companion object {
        fun setPlantHealthStatus(plant: Plant, healthParamsList: List<HealthParameters>): String {

            if (plant.requiredHealthParameters != null && healthParamsList.isNotEmpty()) {
                val requiredHealthParams = plant.requiredHealthParameters
                val recentHealthParams = healthParamsList.last()

                if (requiredHealthParams != null) {
                    if (recentHealthParams.humidity < requiredHealthParams.humidity) {
                        return PlantHealthEnum.WATER_DEFICIENT.healthStats
                    } else if (recentHealthParams.nitrogen < requiredHealthParams.nitrogen
                        || recentHealthParams.phosphorus < requiredHealthParams.phosphorus
                        || recentHealthParams.potassium < requiredHealthParams.potassium
                        || recentHealthParams.ph < requiredHealthParams.ph
                    ) {
                        return PlantHealthEnum.NUTRIENT_DEFICIENT.healthStats
                    } else if (recentHealthParams.temperature < requiredHealthParams.temperature) {
                        return PlantHealthEnum.TEMPERATURE_DEFICIENT.healthStats
                    } else if (recentHealthParams.lightLevel < requiredHealthParams.lightLevel) {
                        return PlantHealthEnum.LIGHT_DEFICIENT.healthStats
                    }
                }
            }
            if (plant.disease != null) {
                return PlantHealthEnum.DISEASED.healthStats
            }
            return PlantHealthEnum.HEALTHY.healthStats
        }
    }
}