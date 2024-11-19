package info.hermiths.chatapp.data.local

import info.hermiths.chatapp.model.MessageEntity
import info.hermiths.chatapp.model.UploadTask
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmInstance {

    val realm: Realm by lazy {
        val config =
            RealmConfiguration.create(
                schema = setOf(
                    MessageEntity::class,
                    UploadTask::class,
                )
            )
        Realm.open(config)
    }
}