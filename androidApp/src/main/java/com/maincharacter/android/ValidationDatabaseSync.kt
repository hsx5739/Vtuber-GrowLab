package com.maincharacter.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase

internal object ValidationDatabaseSync {
    private const val DATABASE_NAME = "main_character.db"

    fun syncState(context: Context, state: PersistedAppState) {
        val database = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null)
        database.use { db ->
            ensureTables(db)
            syncHostState(db, state)
            syncTasks(db, state)
            syncInventory(db, state)
        }
    }

    private fun ensureTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS HostState (
                fortune INTEGER NOT NULL DEFAULT 0,
                vitality INTEGER NOT NULL DEFAULT 50,
                mood INTEGER NOT NULL DEFAULT 50,
                bond INTEGER NOT NULL DEFAULT 0,
                focus INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS TaskInstance (
                task_id TEXT NOT NULL PRIMARY KEY,
                status TEXT NOT NULL DEFAULT 'LOCKED',
                completed_at INTEGER,
                verified_at INTEGER
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS Inventory (
                item_id TEXT NOT NULL PRIMARY KEY,
                item_type TEXT NOT NULL,
                count INTEGER NOT NULL DEFAULT 0,
                data TEXT NOT NULL DEFAULT '{}'
            )
            """.trimIndent()
        )
    }

    private fun syncHostState(db: SQLiteDatabase, state: PersistedAppState) {
        db.execSQL("DELETE FROM HostState")
        db.execSQL(
            "INSERT INTO HostState(fortune, vitality, mood, bond, focus) VALUES (?, ?, ?, ?, ?)",
            arrayOf(state.charm, state.vitality, state.mood, state.bond, state.focus)
        )
    }

    private fun syncTasks(db: SQLiteDatabase, state: PersistedAppState) {
        state.taskStates.forEach { (taskId, taskState) ->
            val dbStatus = when (taskState.status) {
                TaskBoardStatus.COMPLETED.name,
                TaskBoardStatus.CLAIMABLE.name -> "COMPLETED"
                TaskBoardStatus.IN_PROGRESS.name -> "IN_PROGRESS"
                else -> "AVAILABLE"
            }
            val completedAt = if (dbStatus == "COMPLETED") System.currentTimeMillis() else null
            db.execSQL(
                """
                INSERT OR REPLACE INTO TaskInstance(task_id, status, completed_at, verified_at)
                VALUES (?, ?, ?, NULL)
                """.trimIndent(),
                arrayOf(taskId, dbStatus, completedAt)
            )
        }
    }

    private fun syncInventory(db: SQLiteDatabase, state: PersistedAppState) {
        state.itemCounts.forEach { (itemId, count) ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO Inventory(item_id, item_type, count, data)
                VALUES (?, 'item', ?, '{}')
                """.trimIndent(),
                arrayOf(itemId, count)
            )
        }
    }
}
