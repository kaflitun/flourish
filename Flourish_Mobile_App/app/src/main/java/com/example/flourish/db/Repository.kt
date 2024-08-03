package com.example.flourish.db

import io.realm.kotlin.types.RealmObject
import org.mongodb.kbson.ObjectId
import kotlin.reflect.KClass

// Repository class is a generic class to handle database operations for a specific model class (such as PlantDisease, Plant, and other model classes)
// Having a generic repository class helps in reusing the database operations for different model classes
// Instead of writing the same database operations for each model class
class Repository<T> (private val ofType: KClass<T>) where T : RealmObject {

    fun insert(data: T) {
        Database.realm.writeBlocking {
            copyToRealm(data)
        }
    }

    fun getAll() : List<T>{
        return Database.realm.query(ofType).find()
    }

    fun getById(id: ObjectId) : T? {
        return Database.realm.query(ofType, "_id == $0", id).first().find()
    }

    fun deleteById(id: ObjectId){
        val plant = getById(id)

        Database.realm.writeBlocking {
            if (plant != null) {
                findLatest(plant)
                    ?.also { delete(it) }
            }
        }
    }
    fun deleteAll(){
        val plants: List<T> = getAll()

        Database.realm.writeBlocking {
            if (plants.isNotEmpty()) {
                deleteAll()
            }
        }
    }
    fun closeRealm() {
        if (!Database.realm.isClosed()) {
            Database.realm.close()
        }
    }
}