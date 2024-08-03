package com.example.flourish.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

// Plant class is a model class to store a plant
class Plant() : RealmObject{
    @PrimaryKey
    var _id : ObjectId = ObjectId()
    var name: String = ""
    var description: String = ""
    var image: String = ""
    var healthStatus: String = ""
    var requiredWatering: String = ""
    var requiredFeeding: String = ""
    var requiredSoilParameters: String = ""
    var requiredLight: String = ""
    //relationship
    var recommendation: Recommendation? = null
    var requiredHealthParameters: HealthParameters? = null
    var disease: Disease? = null
    var healthParameters: RealmList<HealthParameters> = realmListOf()
    var fertilisingSchedule : FertilisingSchedule? = null
    var wateringSchedule : WateringSchedule? = null
}
