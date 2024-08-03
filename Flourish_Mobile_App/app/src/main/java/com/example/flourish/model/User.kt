package com.example.flourish.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// User class is a model class to store user details
// Currently, this class is not used in the app
// But it can be used in the future to save user data and bind plants to a particular user
class User() : RealmObject {
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var name: String = ""
    var email: String = ""

    var plants: RealmList<Plant> = realmListOf()
}
