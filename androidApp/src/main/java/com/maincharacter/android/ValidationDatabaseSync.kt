package com.maincharacter.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.json.JSONObject

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
        db.execSQL("DELETE FROM Inventory")

        state.inventory.skillCards.forEach { (skillId, skillState) ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO Inventory(item_id, item_type, count, data)
                VALUES (?, 'skill', ?, ?)
                """.trimIndent(),
                arrayOf(
                    skillId,
                    if (skillState.isUnlocked) 1 else 0,
                    JSONObject().apply {
                        put("isUnlocked", skillState.isUnlocked)
                        put("level", skillState.level)
                        put("experience", skillState.experience)
                        put("usageCount", skillState.usageCount)
                        put("lastUsedTime", skillState.lastUsedTime)
                        put("duplicateRule", InventoryCatalog.skillDuplicateConversion)
                    }.toString()
                )
            )
        }

        state.inventory.items.forEach { (itemId, itemState) ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO Inventory(item_id, item_type, count, data)
                VALUES (?, 'item', ?, ?)
                """.trimIndent(),
                arrayOf(
                    itemId,
                    itemState.count,
                    JSONObject().apply {
                        put("isConsumable", itemState.isConsumable)
                        put("isEquipped", itemState.isEquipped)
                        put("lastUsedTime", itemState.lastUsedTime)
                        put("metadata", JSONObject(itemState.metadata))
                    }.toString()
                )
            )
        }

        state.inventory.skins.forEach { (skinId, skinState) ->
            val shardCount = state.inventory.shards[skinId]?.count ?: skinState.shardCount
            db.execSQL(
                """
                INSERT OR REPLACE INTO Inventory(item_id, item_type, count, data)
                VALUES (?, 'skin', ?, ?)
                """.trimIndent(),
                arrayOf(
                    skinId,
                    if (skinState.isUnlocked) 1 else 0,
                    JSONObject().apply {
                        put("isUnlocked", skinState.isUnlocked)
                        put("isEquipped", state.inventory.equippedSkinId == skinId)
                        put("unlockTime", skinState.unlockTime)
                        put("equipTime", skinState.equipTime)
                        put("shardCount", shardCount)
                        put("metadata", JSONObject(skinState.metadata))
                    }.toString()
                )
            )
        }

        state.inventory.shards.forEach { (skinId, shardState) ->
            db.execSQL(
                """
                INSERT OR REPLACE INTO Inventory(item_id, item_type, count, data)
                VALUES (?, 'shard', ?, ?)
                """.trimIndent(),
                arrayOf(
                    "${skinId}_shard",
                    shardState.count,
                    JSONObject().apply {
                        put("skinId", skinId)
                        put("lastUpdateTime", shardState.lastUpdateTime)
                    }.toString()
                )
            )
        }
    }
}
