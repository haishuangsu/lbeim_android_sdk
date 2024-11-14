package info.hermiths.chatapp.data.local

import info.hermiths.chatapp.model.MessageEntity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmInstance {

    val realm: Realm by lazy {
        val config =
            RealmConfiguration.create(
                schema = setOf(
                    MessageEntity::class,
//                    MediaSrc::class,
//                    Thumb::class,
//                    Rsc::class
                )
            )
        Realm.open(config)
    }
}