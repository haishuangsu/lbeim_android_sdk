package info.hermiths.chatapp.data

import info.hermiths.chatapp.model.MessageEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmInstance {

    val realm: Realm by lazy {
        val config = RealmConfiguration.create(schema = setOf(MessageEntity::class))
        Realm.open(config)
    }
}