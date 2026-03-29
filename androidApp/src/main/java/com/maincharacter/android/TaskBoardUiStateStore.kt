package com.maincharacter.android

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal object TaskBoardUiStateStore {
    private val _selectedTab = MutableStateFlow(TaskBoardTab.DAILY)
    val selectedTab: StateFlow<TaskBoardTab> = _selectedTab.asStateFlow()

    fun selectTab(tab: TaskBoardTab) {
        _selectedTab.value = tab
    }
}
