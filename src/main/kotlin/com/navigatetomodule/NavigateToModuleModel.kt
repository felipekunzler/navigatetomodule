package com.navigatetomodule

import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project

class NavigateToModuleModel(project: Project) :
    FilteringGotoByModel<FileType>(project, arrayOf(NavigateToModelContributor())), DumbAware {

    override fun willOpenEditor(): Boolean = false

    override fun saveInitialCheckBoxState(state: Boolean) = Unit

    override fun getFullName(element: Any): String? = getElementName(element)

    override fun loadInitialCheckBoxState(): Boolean = false

    override fun getPromptText(): String = "Enter module name"

    override fun getNotInMessage(): String = "No matches found"

    override fun getCheckBoxName(): String? = null

    override fun getSeparators(): Array<String> = emptyArray()

    override fun filterValueFor(item: NavigationItem): FileType? = null

    override fun getNotFoundMessage(): String = "Module not found"

}
