package com.example.homeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Embedded
import androidx.room.Relation

// @Entity говорит Room:
// "Этот класс — таблица в базе данных"
@Entity(tableName = "members")
data class MemberEntity(

    // @PrimaryKey — первичный ключ таблицы
    // должен быть уникальным для каждой записи
    @PrimaryKey val id: String,

    // имя человека
    val name: String
)

@Entity(
    tableName = "tasks",

    // foreignKeys — объявляем связь таблиц
    foreignKeys = [
        ForeignKey(

            // эта таблица связана с MemberEntity
            entity = MemberEntity::class,

            // колонка родителя
            parentColumns = ["id"],

            // колонка текущей таблицы
            childColumns = ["memberId"],

            // если удалим человека — удалятся все его задачи
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TaskEntity(

    // id задачи
    @PrimaryKey val id: String,

    // ссылка на владельца задачи
    val memberId: String,

    // название задачи
    val title: String,

    // дедлайн в миллисекундах
    // Room не умеет хранить LocalDateTime напрямую
    val deadline: Long,

    // выполнена ли задача
    val isDone: Boolean,

    // время выполнения (nullable)
    val completedAt: Long?
)


// Связь один-ко-многим
data class MemberWithTasks(

    // @Embedded — просто вложенный объект
    // поля member станут колонками результата
    @Embedded val member: MemberEntity,

    // @Relation — автоматическая связь таблиц
    @Relation(
        parentColumn = "id",       // колонка родителя
        entityColumn = "memberId"  // колонка ребёнка
    )
    val tasks: List<TaskEntity>
)