package com.example.flourish

import com.example.flourish.enum.PlantHealthEnum
import com.example.flourish.helper.PlantHealthStatusManager
import com.example.flourish.model.Disease
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import org.junit.Assert.assertEquals
import org.junit.Test

class PlantHealthStatusManagerUnitTests {
    @Test
    fun setPlantHealthStatus_returnsWaterDeficientWhenHumidityIsLow() {
        val plant = Plant().apply {
            requiredHealthParameters = HealthParameters().apply { humidity = 50f }
        }
        val healthParamsList = listOf(HealthParameters().apply { humidity = 40f })
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.WATER_DEFICIENT.healthStats, result)
    }

    @Test
    fun setPlantHealthStatus_returnsNutrientDeficientWhenNutrientsAreLow() {
        val plant = Plant().apply {
            requiredHealthParameters = HealthParameters().apply {
                nitrogen = 50f; phosphorus = 50f; potassium = 50f; ph = 7f
            }
        }
        val healthParamsList = listOf(HealthParameters().apply {
            nitrogen = 40f; phosphorus = 40f; potassium = 40f; ph = 6f
        })
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.NUTRIENT_DEFICIENT.healthStats, result)
    }

    @Test
    fun setPlantHealthStatus_returnsTemperatureDeficientWhenTemperatureIsLow() {
        val plant = Plant().apply {
            requiredHealthParameters = HealthParameters().apply { temperature = 20f }
        }
        val healthParamsList = listOf(HealthParameters().apply { temperature = 15f })
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.TEMPERATURE_DEFICIENT.healthStats, result)
    }

    @Test
    fun setPlantHealthStatus_returnsLightDeficientWhenLightLevelIsLow() {
        val plant = Plant().apply {
            requiredHealthParameters = HealthParameters().apply { lightLevel = 100f }
        }
        val healthParamsList = listOf(HealthParameters().apply { lightLevel = 80f })
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.LIGHT_DEFICIENT.healthStats, result)
    }

    @Test
    fun setPlantHealthStatus_returnsDiseasedWhenPlantHasDisease() {
        val plant = Plant().apply { disease = Disease() }
        val healthParamsList = emptyList<HealthParameters>()
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.DISEASED.healthStats, result)
    }

    @Test
    fun setPlantHealthStatus_returnsHealthyWhenNoIssues() {
        val plant = Plant().apply {
            requiredHealthParameters = HealthParameters().apply {
                humidity = 50f; nitrogen = 50f; phosphorus = 50f; potassium = 50f; ph =
                7f; temperature = 20f; lightLevel = 100f
            }
        }
        val healthParamsList = listOf(HealthParameters().apply {
            humidity = 50f; nitrogen = 50f; phosphorus = 50f; potassium = 50f; ph =
            7f; temperature = 20f; lightLevel = 100f
        })
        val result = PlantHealthStatusManager.setPlantHealthStatus(plant, healthParamsList)
        assertEquals(PlantHealthEnum.HEALTHY.healthStats, result)
    }

}