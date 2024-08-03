package com.example.flourish.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// WateringSchedule class is a model class to store watering schedule for a plant
class WateringSchedule() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var weekDays: RealmList<String> = realmListOf()
    var time: String? = ""
}