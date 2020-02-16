package com.navigatetomodule

import com.intellij.ide.FileSelectInContext
import com.intellij.ide.SelectInManager
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ModuleRootManager
import javax.swing.Icon

class ModuleNavigationItem(private val module: Module) : NavigationItem {

    override fun navigate(requestFocus: Boolean) {
        val virtualFile = ModuleRootManager.getInstance(module).contentRoots[0]
        val context = FileSelectInContext(module.project, virtualFile)
        SelectInManager.findSelectInTarget(SelectInManager.PROJECT, module.project).selectIn(context, true)
    }

    override fun getPresentation(): ItemPresentation? = RequestMappingItemPresentation()

    override fun canNavigate(): Boolean = true

    override fun getName(): String = module.name

    override fun canNavigateToSource(): Boolean = false

    private inner class RequestMappingItemPresentation : ItemPresentation {

        override fun getPresentableText() = module.name

        override fun getLocationString(): String? = null

        override fun getIcon(b: Boolean): Icon? = null
    }

}
