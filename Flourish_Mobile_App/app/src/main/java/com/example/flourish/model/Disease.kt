package com.example.flourish.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// Disease class is a model class to store the disease of a plant
class Disease() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var heading: String = ""
    var description: String = ""
    var symptoms: String = ""
    var treatment: String = ""
    var causes: String = ""
    var image: String = ""
}
