package com.navigatetomodule

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.popup.AbstractPopup
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class NavigateToModulePopup(private val project: Project) {

    private val searchField = JTextField()
    private val resultsList = JBList<ModuleNavigationItem>()
    private val recentList = JBList<String>()

    private val moduleManager = ModuleManager.getInstance(project)
    private val recentService = project.getService(RecentModulesService::class.java)

    private var popup: JBPopup? = null

    fun show() {
        val panel = buildUI()

        // Default size only for FIRST run (DimensionService has nothing yet)
        panel.putClientProperty(AbstractPopup.FIRST_TIME_SIZE, Dimension(900, 600))

        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, searchField)
            .setTitle("Navigate To Module")
            .setResizable(true)
            .setMovable(true)
            .setRequestFocus(true)
            .setCancelOnClickOutside(true)
            .setDimensionServiceKey(project, "NavigateToModulePopup", false)
            .createPopup()

        popup!!.showCenteredInCurrentWindow(project)

        SwingUtilities.invokeLater { searchField.requestFocusInWindow() }
    }


    private fun buildUI(): JComponent {

        loadAllModules()
        loadRecents()
        setupRenderers()
        setupListeners()
        installLeftRightNavigation()

        val leftPanel = JPanel(BorderLayout())
        leftPanel.border = JBUI.Borders.empty(8)
        leftPanel.add(searchField, BorderLayout.NORTH)
        leftPanel.add(JBScrollPane(resultsList), BorderLayout.CENTER)

        val rightPanel = JPanel(BorderLayout())
        rightPanel.border = JBUI.Borders.empty(8)

        val recentLabel = JLabel("Recent Modules")
        recentLabel.border = JBUI.Borders.emptyBottom(6)

        rightPanel.add(recentLabel, BorderLayout.NORTH)
        rightPanel.add(JBScrollPane(recentList), BorderLayout.CENTER)

        val split = JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel
        )

        split.resizeWeight = 0.7

        split.registerKeyboardAction(
            { cycleFocusForward() },
            KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        )

        return split
    }

    private fun setupRenderers() {

        resultsList.cellRenderer =
            SimpleListCellRenderer.create<ModuleNavigationItem> { label, value, _ ->
                label.text = value.name
                label.border = JBUI.Borders.empty(4, 8)
            }

        recentList.cellRenderer =
            SimpleListCellRenderer.create<String> { label, value, _ ->
                label.text = value
                label.border = JBUI.Borders.empty(8, 12)
                label.font = label.font.deriveFont(Font.BOLD)
            }
    }

    private fun setupListeners() {

        // Live filtering
        searchField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = filter()
            override fun removeUpdate(e: DocumentEvent) = filter()
            override fun changedUpdate(e: DocumentEvent) = filter()
        })

        // ENTER from search
        searchField.addActionListener {
            navigateSelected()
        }

        // ↓ Search -> Results
        searchField.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_DOWN) {
                    if (resultsList.model.size > 0) {
                        resultsList.selectedIndex = 0
                        resultsList.requestFocusInWindow()
                    }
                }
            }
        })

        // RESULTS LIST
        resultsList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {

                // ↑ from first result -> Search
                if (e.keyCode == KeyEvent.VK_UP && resultsList.selectedIndex == 0) {
                    searchField.requestFocusInWindow()
                }

                if (e.keyCode == KeyEvent.VK_ENTER) {
                    navigateSelected()
                }
            }
        })

        resultsList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    navigateSelected()
                }
            }
        })

        // RECENT LIST
        recentList.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {

                if (e.keyCode == KeyEvent.VK_ENTER) {
                    val name = recentList.selectedValue ?: return
                    openModuleByName(name)
                }
            }
        })

        recentList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) {
                    val name = recentList.selectedValue ?: return
                    openModuleByName(name)
                }
            }
        })
    }

    private fun cycleFocusForward() {
        when {
            searchField.isFocusOwner -> {
                if (resultsList.model.size > 0) {
                    resultsList.requestFocusInWindow()
                }
            }

            resultsList.isFocusOwner -> {
                if (recentList.model.size > 0) {
                    recentList.requestFocusInWindow()
                }
            }

            recentList.isFocusOwner -> {
                searchField.requestFocusInWindow()
            }
        }
    }

    private fun installLeftRightNavigation() {

        val actionToRecent = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                if (recentList.model.size > 0) {
                    if (recentList.selectedIndex == -1) {
                        recentList.selectedIndex = 0
                    }
                    recentList.requestFocusInWindow()
                }
            }
        }

        val actionToSearch = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                searchField.requestFocusInWindow()
            }
        }

        // Bind RIGHT globally inside popup
        listOf(searchField, resultsList).forEach { comp ->
            comp.inputMap.put(
                KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
                "focusRecent"
            )
            comp.actionMap.put("focusRecent", actionToRecent)
        }

        // Bind LEFT on recent
        recentList.inputMap.put(
            KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),
            "focusSearch"
        )
        recentList.actionMap.put("focusSearch", actionToSearch)
    }

    private fun filter() {
        val query = searchField.text.trim().lowercase()

        val modules = moduleManager.modules
            .filter { it.name.lowercase().contains(query) }
            .map { ModuleNavigationItem(it) }

        resultsList.setListData(modules.toTypedArray())

        if (modules.isNotEmpty()) {
            resultsList.selectedIndex = 0
        }
    }

    private fun loadAllModules() {
        val modules = moduleManager.modules
            .map { ModuleNavigationItem(it) }

        resultsList.setListData(modules.toTypedArray())

        if (modules.isNotEmpty()) {
            resultsList.selectedIndex = 0
        }
    }

    private fun loadRecents() {
        recentList.setListData(recentService.getRecent().toTypedArray())
    }

    private fun navigateSelected() {
        val selected = resultsList.selectedValue ?: return
        selected.navigate(true)

        recentService.add(selected.name)
        loadRecents()

        popup?.cancel()
    }

    private fun openModuleByName(name: String) {
        val module = moduleManager.modules.firstOrNull { it.name == name } ?: return
        val item = ModuleNavigationItem(module)

        item.navigate(true)
        recentService.add(name)
        loadRecents()

        popup?.cancel()
    }
}