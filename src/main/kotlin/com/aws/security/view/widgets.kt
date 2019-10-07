package com.aws.security.view

import com.aws.security.*
import com.aws.security.controllers.FilterController
import com.aws.security.controllers.TableController
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import tornadofx.*


class WidgetStyles : StylesBase() {

    init {
        filterContainer {
            maxHeight = 240.px
            backgroundColor += c(tabContentBackGroundColor)
        }
        filterlabel {
            fontFamily = "monospaced"
            fontSize = 18.px
            prefWidth = 280.px
            maxWidth = 280.px
            minHeight = 40.px
            alignment = Pos.CENTER
            textFill = c("white")
            fontFamily = "monospaced"
            borderColor += box(c("blue"))
            backgroundColor += c(darkBackgroundColor)
        }
        filterButton {
            prefWidth = 260.px
            maxWidth = 260.px
            fontSize = 18.px
            textFill = c("white")
            fontFamily = "monospaced"
            borderColor += box(c("blue"))
            backgroundColor += c(darkBackgroundColor)
        }

        internalListView {
            fontFamily = "monospaced"
            fontSize = 16.px
            prefWidth = 400.px
            maxWidth = 400.px
            borderColor += box(c("blue"))
            backgroundColor += c("transparent")
        }
    }

    companion object {
        val filterContainer by cssclass()
        val filterlabel by cssclass()
        val internalListView by cssclass()
        val filterButton by cssclass()
    }
}

class Filters : Fragment() {

    private val filterController: FilterController by inject()

    override val root = vbox(20) {
        addClass(WidgetStyles.filterContainer)
        hbox(20) {

            if(!isFilterOnly) {
                add(makeFilterBox(FilterEventType.FILTER_IP, AppStore.SELECTED_IP, filterController.iPWhiteList))
            }
            add(
                makeFilterBox(
                    FilterEventType.FILTER_TYPE, AppStore.SELECTED_TYPE,
                    FXCollections.observableArrayList(EventType.values().toList())
                )
            )
        }
    }

    fun <T> makeFilterBox(actionMsg: FilterEventType, prop: SimpleObjectProperty<T>, list: ObservableList<T>): VBox {
        return vbox(20) {
            add(
                InternalListView(prop, list).apply {
                    addClass(WidgetStyles.internalListView)
                    HBox.setMargin(this.root, insets(10, 0, 0, 10))
                })
            button(actionMsg.display) {
                shortcut("Alt+S")
                action {
                    fire(FilterEvent(actionMsg))
                    close()
                }
                addClass(WidgetStyles.filterButton)
            }
        }
    }

    companion object {
        var isFilterOnly = true
    }
}

class Process : View() {
    val dataController: TableController by inject()

    override val root = vbox(10) {
        add(
            InternalListView(
                AppStore.SELECTED_SCHEDULE,
                FXCollections.observableArrayList(ScheduleType.values().toList())
            ).apply {
                addClass(WidgetStyles.internalListView)
                HBox.setMargin(this.root, insets(10, 0, 0, 10))
            })
        button("Process Selected") {
            shortcut("Alt+S")
            action {
                runAsyncWithProgress {
                    if(AppStore.SELECTED_SCHEDULE.name.equals(ScheduleType.ONE_OFF.name)) {
                        dataController.processData()
                    } else {
                        dataController.processScheduledData()
                        true
                    }
                } ui {
                    close()
                } fail {
                    AppArtifacts.appLogger.info("fails in Process widget get remote data ${it.localizedMessage}")
                }
            }
        }
    }
}

class IPWhiteList : View() {
    private val filterController: FilterController by inject()
    val model: MonitorDataModel by inject()
    lateinit var ipfield: TextField
    override val root = form() {
        fieldset("IP Whitelist", labelPosition = Orientation.VERTICAL) {
            this.legend.apply {
                style {
                    textFill = c("white")
                }
            }
            field("Enter IP ", Orientation.VERTICAL) {
                ipfield = textfield(AppStore.ENTERED_IP)
                this.label.apply {
                    style {
                        textFill = c("white")
                    }
                }
            }
            button("enter") {
                addClass(LeftBarStyles.leftBarButton)
                action {
                    filterController.updateWhiteList()
                }
            }
        }
        add(
            InternalListView(AppStore.SELECTED_IP, filterController.iPWhiteList)
                .apply {
                    addClass(LeftBarStyles.leftBarInternalListView)
                    VBox.setMargin(this.root, insets(4, 0, 0, 0))
                }
        )

        ipfield.textProperty().bindBidirectional(model.ip)

    }
}

class InternalListView<T>(prop: SimpleObjectProperty<T>, dataList: ObservableList<T>) : View() {

    override val root = listview<T>(dataList) {
        bindSelected(prop)
        placeholder = label("no data")
        minHeightProperty().bind(Bindings.size(items).multiply(30))
        prefHeightProperty().bind(Bindings.size(items).multiply(30))
        maxHeightProperty().bind(Bindings.size(items).multiply(30))
        prefWidth = 240.0

    }
}