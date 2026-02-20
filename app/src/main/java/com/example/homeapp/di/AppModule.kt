package com.example.homeapp.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.homeapp.data.HomeRepositoryImpl
import com.example.homeapp.data.local.AppDatabase
import com.example.homeapp.data.local.FamilyDao
import com.example.homeapp.data.remote.AdviceApi
import com.example.homeapp.domain.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHomeRepository(
        impl: HomeRepositoryImpl
    ): HomeRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "home_app_db"
        ).build()
    }

    @Provides
    fun provideFamilyDao(db: AppDatabase): FamilyDao = db.familyDao()

    @Provides
    @Singleton
    fun provideAdviceApi(): AdviceApi {
        return Retrofit.Builder()
            .baseUrl("https://api.adviceslip.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdviceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}