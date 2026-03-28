package com.maincharacter.shared.database.legacy

import com.maincharacter.shared.model.CompanionPresentation
import com.maincharacter.shared.model.GachaState
import com.maincharacter.shared.model.HostState
import com.maincharacter.shared.model.Inventory
import com.maincharacter.shared.model.SignInState
import com.maincharacter.shared.model.TaskInstance
import com.maincharacter.shared.model.Wallet

interface MainCharacterDatabase {
    suspend fun getHostState(): HostState?
    suspend fun saveHostState(state: HostState)

    suspend fun getWallet(): Wallet?
    suspend fun saveWallet(wallet: Wallet)

    suspend fun getInventory(): Inventory?
    suspend fun saveInventoryItem(
        itemId: String,
        itemType: String,
        count: Int,
        data: String = "{}"
    )

    suspend fun getTaskInstance(taskId: String): TaskInstance?
    suspend fun saveTaskInstance(instance: TaskInstance)

    suspend fun getSignInState(): SignInState?
    suspend fun saveSignInState(state: SignInState)

    suspend fun getGachaState(poolId: String): GachaState?
    suspend fun saveGachaState(state: GachaState)

    suspend fun getCompanionPresentation(): CompanionPresentation?
    suspend fun saveCompanionPresentation(presentation: CompanionPresentation)
}
