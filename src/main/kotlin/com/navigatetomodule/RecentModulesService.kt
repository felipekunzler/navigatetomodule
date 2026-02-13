package com.navigatetomodule

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(
    name = "NavigateToModuleState",
    storages = [Storage("navigateToModule.xml")]
)
class RecentModulesService : PersistentStateComponent<RecentModulesService.State> {

    data class State(
        var recent: MutableList<String> = mutableListOf(),
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getRecent(): List<String> = state.recent

    fun add(name: String) {
        state.recent.remove(name)
        state.recent.add(0, name)

        if (state.recent.size > 20) {
            state.recent = state.recent.take(20).toMutableList()
        }
    }
}