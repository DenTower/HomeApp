package com.example.homeapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Embedded
import androidx.room.Relation

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey val id: String,
    val name: String
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE // Если удалим человека, удалятся и его задачи
        )
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val memberId: String,
    val title: String,
    val deadline: Long, // Храним время в миллисекундах
    val isDone: Boolean,
    val completedAt: Long?
)

// Связка 1-ко-многим (Один член семьи -> Много задач)
data class MemberWithTasks(
    @Embedded val member: MemberEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "memberId"
    )
    val tasks: List<TaskEntity>
)