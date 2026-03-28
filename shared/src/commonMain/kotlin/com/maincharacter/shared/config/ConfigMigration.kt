package com.maincharacter.shared.config.legacy

class ConfigMigration {

    data class MigrationResult(
        val migratedConfig: Any,
        val warnings: List<String> = emptyList()
    )

    fun migrate(
        config: Any,
        fromVersion: Int,
        toVersion: Int
    ): MigrationResult {
        if (fromVersion == toVersion) {
            return MigrationResult(config)
        }

        return MigrationResult(
            migratedConfig = config,
            warnings = listOf("No migration rules are configured yet.")
        )
    }
}
