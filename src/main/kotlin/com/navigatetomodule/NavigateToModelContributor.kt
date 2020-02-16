package com.navigatetomodule

import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project

class NavigateToModelContributor(
    private var navigationItems: List<ModuleNavigationItem> = emptyList()
) : ChooseByNameContributor {

    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String?> {
        navigationItems = ModuleManager.getInstance(project).modules.map { m -> ModuleNavigationItem(m) }
        return navigationItems.map { item -> item.name }.toTypedArray()
    }

    override fun getItemsByName(
        name: String?,
        pattern: String?,
        project: Project?,
        includeNonProjectItems: Boolean
    ): Array<ModuleNavigationItem> {
        return navigationItems.filter { item -> item.name == name }.toTypedArray()
    }

}