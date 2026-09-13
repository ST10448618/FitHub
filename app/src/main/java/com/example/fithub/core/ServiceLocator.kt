package com.example.fithub.core

import android.content.Context
import com.example.fithub.data.local.FitHubDatabase
import com.example.fithub.data.repository.AuthRepositoryImpl
import com.example.fithub.data.repository.GoalRepositoryImpl
import com.example.fithub.data.repository.UserRepositoryImpl
import com.example.fithub.data.repository.WeightRepositoryImpl
import com.example.fithub.domain.repository.AuthRepository
import com.example.fithub.domain.repository.GoalRepository
import com.example.fithub.domain.repository.UserRepository
import com.example.fithub.domain.repository.WeightRepository
import kotlin.getValue

/**
 * Application-wide service locator.
 * Initialise once from FitHubApplication.onCreate().
 * Add new repositories here as they are built.
 */
object ServiceLocator {

    private lateinit var appContext: Context

    lateinit var database: FitHubDatabase
        private set

    // Repositories (lazy — only created when first accessed)
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl() }
    val userRepository: UserRepository by lazy { UserRepositoryImpl(database.userProfileDao()) }
    val weightRepository: WeightRepository by lazy { WeightRepositoryImpl(database.weightEntryDao(), database.userProfileDao()) }
    val goalRepository: GoalRepository by lazy { GoalRepositoryImpl(database.goalDao()) }

    fun init(context: Context) {
        appContext = context.applicationContext
        database = FitHubDatabase.getInstance(appContext)
    }

    fun appContext(): Context = appContext
}