package com.example.flourish.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// FertilisingSchedule class is a model class to store fertilising schedule for a plant
class FertilisingSchedule() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var date: String? = ""
    var time: String? = ""
}