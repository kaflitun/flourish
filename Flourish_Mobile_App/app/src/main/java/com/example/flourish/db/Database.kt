package com.example.flourish.db

import com.example.flourish.model.Disease
import com.example.flourish.model.FertilisingSchedule
import com.example.flourish.model.HealthParameters
import com.example.flourish.model.Plant
import com.example.flourish.model.Recommendation
import com.example.flourish.model.User
import com.example.flourish.model.WateringSchedule
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

// Database object is a singleton class to create and configure a Realm database instance
object Database{

    private val config = RealmConfiguration.Builder(schema = setOf(User::class, Plant::class, Recommendation::class,
        HealthParameters::class, Disease::class, FertilisingSchedule::class, WateringSchedule::class)).schemaVersion(9).build()

    val realm: Realm  = Realm.open(config)
}