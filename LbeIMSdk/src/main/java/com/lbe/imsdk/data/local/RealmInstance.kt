package com.lbe.imsdk.data.local

import com.lbe.imsdk.model.LocalMediaFile
import com.lbe.imsdk.model.MessageEntity
import com.lbe.imsdk.model.UploadTask
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