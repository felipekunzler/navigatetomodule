package com.navigatetomodule

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT
import com.intellij.openapi.project.DumbAware

class NavigateToModuleAction : GotoActionBase(), DumbAware {

    override fun gotoActionPerformed(e: AnActionEvent) {
        val project = e.getData(PROJECT) ?: return
        showNavigationPopup(e, NavigateToModuleModel(project), GoToModuleActionCallBack(), false)
    }

    private class GoToModuleActionCallBack : GotoActionBase.GotoActionCallback<String>() {
        override fun elementChosen(popup: ChooseByNamePopup, element: Any) {
            if (element is ModuleNavigationItem) {
                element.navigate(true)
            }
        }
    }

}