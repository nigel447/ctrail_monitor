package com.aws.security.view

import com.aws.security.*
import com.aws.security.AppState.TABLE_TIME_FORMAT
import com.aws.security.controllers.DataCache
import com.aws.security.controllers.FilterController
import com.aws.security.controllers.TableController
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import tornadofx.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable

// https://edvin.gitbooks.io/tornadofx-guide/part1/11.%20Editing%20Models%20and%20Validation.html
// https://slar.se/tornadofxexposed-pt-3-adding-editing-and-removing-rows.html

abstract class TabsBase : View() {


}


// https://github.com/edvin/tornadofx/issues/134


class EventsMonitor : TabsBase() {

    private val filterController: FilterController by inject()
    val dataController: TableController by inject()
    val model: MonitorDataModel by inject()
    lateinit var dataTable: TableView<MonitorData>
    lateinit var filterButton: Button
    lateinit var processButton: Button
    lateinit var logInMssg: Label
    lateinit var scheduledMssg: Label

    private val cellHeight = 15.0
    private val adjustHeight = 20.0

    override val root = vbox(20) {

        hbox(20) {

            processButton = button("Process") {
                action {
                    openInternalWindow<Process>(modal = false)
                }

                addClass(WidgetStyles.filterButton)
            }
            filterButton = button("Show Filters") {
                action {
                    processFiltersView()
                }
                addClass(WidgetStyles.filterButton)
            }

            scheduledMssg = label() {
                isManaged = false
                isVisible = false
                addClass(WidgetStyles.filterlabel)
            }
            VBox.setMargin(this, insets(10, 0, 0, 10))
        }

        dataTable = tableview(DataCache.data) {
            column("Type", MonitorData::typeProperty)
            column("Name", MonitorData::eventNameProperty)
            column("Source", MonitorData::eventSourceProperty)
            column("IP", MonitorData::sourceIPAddressProperty)
            column("Principal", MonitorData::principalIdProperty)
            column("Time", MonitorData::eventTimeProperty).cellFormat {
                val localDateTime = LocalDateTime.ofInstant(Instant.parse(it), ZoneId.of("UTC"))
                text = localDateTime.format(TABLE_TIME_FORMAT)

            }
            column("User Agent", MonitorData::userAgentProperty)
            columnResizePolicy = CONSTRAINED_RESIZE_POLICY
            placeholder = label() {
                style {
                    backgroundColor += c("transparent")
                }
            }
            bindSelected(model)
            style {
                minHeight = 80.px
            }
        }

        logInMssg = label("Please Login") {
            isVisible = true
            VBox.setMargin(this, insets(20, 0, 0, 10))
        }

        logInMssg.visibleProperty().bind(!AppStore.AUTHSTATE)
        logInMssg.managedProperty().bind(!AppStore.AUTHSTATE)

        processButton.visibleProperty().bind(AppStore.AUTHSTATE)
        processButton.managedProperty().bind(AppStore.AUTHSTATE)

        filterButton.visibleProperty().bind(AppStore.AUTHSTATE)
        filterButton.managedProperty().bind(AppStore.AUTHSTATE)

        dataTable.visibleProperty().bind(AppStore.AUTHSTATE)
        dataTable.managedProperty().bind(AppStore.AUTHSTATE)

        dataTable.prefHeightProperty().bind(Bindings.size(DataCache.data).multiply(cellHeight))
        dataTable.maxHeightProperty().bind(Bindings.size(DataCache.data).multiply(cellHeight))

        this.prefHeightProperty().bind(Bindings.size(DataCache.data).multiply(adjustHeight))
        this.maxHeightProperty().bind(Bindings.size(DataCache.data).multiply(adjustHeight))

        AppStore.IS_SCHEDLED.addListener { obsv ->
            if (AppStore.IS_SCHEDLED.get()) {
                processButton.visibleProperty().unbind()
                Platform.runLater {
                    processButton.managedProperty().unbind()
                    processButton.isVisible = false
                    processButton.isManaged = false
                    scheduledMssg.isVisible = true
                    scheduledMssg.isManaged = true
                    scheduledMssg.text = AppStore.SELECTED_SCHEDULE.get().mssg
                }
            } else {
                processButton.isVisible = false
                processButton.isManaged = false
                scheduledMssg.isVisible = false
                scheduledMssg.isManaged = false
            }
        }

        subscribe<DataRefreshEvent> { _ ->
            AppArtifacts.appLogger.info("EventsMonitor subscribe FilterEvent data.size ${DataCache.data.size}")
            Platform.runLater { dataTable.refresh() }
        }
    }

    fun processFiltersView() {
        Filters.isFilterOnly = filterController.iPWhiteList.isEmpty()
        openInternalWindow<Filters>(modal = false)
    }
}