package com.souschef.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import org.koin.dsl.module

/**
 * Networking / Firebase module.
 * Provides FirebaseAuth and FirebaseFirestore singletons.
 * Offline persistence is enabled via the persistent disk cache.
 */
val networkingModule = module {

    single {
        FirebaseFirestore.getInstance().apply {
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
        }
    }

    single { FirebaseAuth.getInstance() }
}

