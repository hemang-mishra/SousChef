package com.souschef.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.souschef.data.repository.AuthRepositoryImpl
import com.souschef.domain.repository.AuthRepository
import com.souschef.feature.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Firebase module - provides Firebase instances
 */
val firebaseModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
}

/**
 * Repository module - provides repository implementations
 */
val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}

/**
 * ViewModel module - provides ViewModels
 */
val viewModelModule = module {
    viewModel { AuthViewModel(get()) }
}

/**
 * All app modules combined for easy initialization
 */
val appModules = listOf(
    firebaseModule,
    repositoryModule,
    viewModelModule
)

