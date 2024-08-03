package com.example.flourish.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// Recommendation class is a model class to store recommendation for a plant
class Recommendation() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var heading: String = ""
    var text: String = ""
    var dateTime: String? = ""
}
