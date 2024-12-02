package info.hermiths.lbesdk.data.local

import info.hermiths.lbesdk.model.LocalMediaFile
import info.hermiths.lbesdk.model.MessageEntity
import info.hermiths.lbesdk.model.UploadTask
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmInstance {

    val realm: Realm by lazy {
        val config = RealmConfiguration.create(
            schema = setOf(
                MessageEntity::class,
                LocalMediaFile::class,
                UploadTask::class,
            )
        )
        Realm.open(config)
    }
}