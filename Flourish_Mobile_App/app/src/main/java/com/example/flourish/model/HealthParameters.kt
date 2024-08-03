package com.example.flourish.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// HealthParameters class is a model class to store the health parameters of a plant
class HealthParameters() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var nitrogen: Float = 0.0F // mg/kg
    var phosphorus: Float = 0.0F // mg/kg
    var potassium: Float = 0.0F // mg/kg
    var ph: Float = 0.0F // pH
    var humidity: Float = 0.0F // %
    var temperature: Float = 0.0F // °C
    var lightLevel: Float = 0.0F // lux
    var dateTime: String? = ""
}
