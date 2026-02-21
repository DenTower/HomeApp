package com.example.homeapp.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow


// Интрфейс с запросами к бд
@Dao
interface FamilyDao {
    @Transaction
    @Query("SELECT * FROM members")
    fun getMembersWithTasks(): Flow<List<MemberWithTasks>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteMember(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    @Query("UPDATE tasks SET isDone = :isDone, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isDone: Boolean, completedAt: Long?)
}