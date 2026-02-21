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

// @Module — говорит системе: «В этом файле лежат инструкции по созданию инструментов».
// @InstallIn(SingletonComponent::class) — означает, что эти инструменты создаются
// один раз и живут всё время, пока открыто приложение (они общие для всех экранов).
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // @Provides — помечает метод как «рецепт».
    // @Singleton — говорит, что нужно создать только ОДИН экземпляр этого объекта на всё приложение.
    @Provides
    @Singleton
    fun provideHomeRepository(
        impl: HomeRepositoryImpl
    ): HomeRepository {
        // Связываем интерфейс (общие правила) с его реальной логикой (реализацией).
        return impl
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        // Создаем базу данных Room — это как Excel-таблица внутри телефона,
        // где будут храниться все данные приложения (название файла: "home_app_db").
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "home_app_db"
        ).build()
    }

    @Provides
    fun provideFamilyDao(db: AppDatabase): FamilyDao = db.familyDao()
    // DAO — это «пульт управления» базой данных. Через него мы будем
    // добавлять, удалять или изменять данные.

    @Provides
    @Singleton
    fun provideAdviceApi(): AdviceApi {
        // Настраиваем Retrofit — это «телефонный аппарат» для звонков в интернет.
        // Здесь мы указываем адрес сервера (базовый URL) и говорим,
        // что данные из интернета нужно автоматически превращать в понятные приложению объекты (Gson).
        return Retrofit.Builder()
            .baseUrl("https://api.adviceslip.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AdviceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        // WorkManager — это «секретарь-планировщик».
        // Он отвечает за выполнение задач в фоновом режиме (например, отправку уведомлений),
        // даже если пользователь закрыл приложение.
        return WorkManager.getInstance(context)
    }
}