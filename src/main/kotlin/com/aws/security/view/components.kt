package com.aws.security.view

import com.aws.security.*
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import tornadofx.*
import java.awt.Font


class LeftBarStyles : StylesBase() {

    init {
        leftBar {
            minWidth = 260.px
            backgroundColor += c(leftBarBackgroundColor)
            borderColor += box(c(darkBackgroundColor))
            // top left bottom right  not usual top right bottom left
            borderWidth += box(8.px, 8.px, 3.px, 8.px)
            // usual t r b l
            borderRadius += box(20.0.px, 20.0.px, 2.px, 2.0.px)
            backgroundRadius += box(26.0.px, 26.0.px, 5.px, 5.0.px)
        }
        leftBarButton {
            fontFamily = "monospaced"
            fontSize = 16.px
            textFill = c("white")
            minWidth = 110.px
            minHeight = 30.px
            borderColor += box(c("#729EBF"))
            borderWidth += box(1.px)
            borderRadius += box(10.0.px)
            backgroundRadius += box(10.0.px)
            backgroundColor += c(darkBackgroundColor)
        }

        ctrlLabel {
            fontFamily = "monospaced"
            fontSize = 22.px
            textFill = c("white")
        }
        ctrlField {
            fontFamily = "monospaced"
            fontSize = 18.px
            minWidth = 160.px
            minHeight = 34.px
        }
        submitField {
            fontFamily = "monospaced"
            fontSize = 20.px
            textFill = c("white")
            minWidth = 140.px
            minHeight = 24.px
        }

        validationLabel {
            fontFamily = "monospaced"
            fontWeight = FontWeight.BOLD
            textFill = c("maroon")
            fontSize = 18.px
        }

        filterFeedback {
            fontFamily = "monospaced"
            fontSize = 16.px
            textFill = c("white")
        }

        filterFeedbackValue {
            fontFamily = "monospaced"
            fontSize = 14.px
            textFill = c("crimson")
            alignment = Pos.CENTER_LEFT
        }

        leftBarInternalListView {
            fontFamily = "monospaced"
            fontSize = 16.px
            prefWidth = 230.px
            maxWidth = 230.px
            backgroundColor += c("transparent")
        }
    }

    companion object {
        val leftBar by cssclass()
        val leftBarButton by cssclass()
        val ctrlLabel by cssclass()
        val ctrlField by cssclass()
        val submitField by cssclass()
        val filterFeedback by cssclass()
        val filterFeedbackValue by cssclass()
        val leftBarInternalListView by cssclass()
        val validationLabel by cssclass()

    }
}

class Header : View() {
    override val root = borderpane {
        left = label("CTrail") {
            addClass(Styles.headerLabel)
        }
    }
}

class LeftBar : View() {

    private lateinit var logInContainer: Form
    private lateinit var logOut: Button
    private val cntrlLabeHOffset = 10.0
    private lateinit var filterState: GridPane

    val model: MonitorDataModel by inject()
    private val loginModel = ViewModel()
    private val username = loginModel.bind { AppStore.username }
    private val password = loginModel.bind { SimpleStringProperty()}
    private val paraphrase = loginModel.bind { AppStore.paraphrase }

    private lateinit var errorMssg: Label
    private val ipList = find(IPWhiteList::class)

    override val root = vbox(10) {
        logInContainer = form() {
            fieldset(labelPosition = Orientation.VERTICAL) {
                val unameField = field("Username") {
                    textfield(username) {
                        addClass(LeftBarStyles.ctrlField)
                    }.required()
                }
                unameField.label.apply {
                    addClass(LeftBarStyles.ctrlLabel)
                }
                HBox.setMargin(unameField.label, insets(0, 0, 0, cntrlLabeHOffset))

                val psswdField = field("Password") {
                    passwordfield(password) {
                        addClass(LeftBarStyles.ctrlField)
                    }.required()
                }
                psswdField.label.apply {
                    addClass(LeftBarStyles.ctrlLabel)
                }
                HBox.setMargin(psswdField.label, insets(0, 0, 0, cntrlLabeHOffset))
                errorMssg = label() {
                    isVisible = false
                    isManaged = false
                    addClass(LeftBarStyles.validationLabel)
                }

                val paraphraseField = field("Paraphrase") {
                    passwordfield(paraphrase) {
                        addClass(LeftBarStyles.ctrlField)
                    }.required()
                }
                paraphraseField.label.apply {
                    addClass(LeftBarStyles.ctrlLabel)
                }
                HBox.setMargin(paraphraseField.label, insets(0, 0, 0, cntrlLabeHOffset))

                val submit = button("Log in") {
                    enableWhen(loginModel.valid)
                    isDefaultButton = true
                    useMaxWidth = true
                    action {
                        runAsyncWithProgress {
                            // this is an rx java subject
                            if (KryptoProvider.basicPasswordCheck(password.value)) {
                                KryptoStore.ALIAS_PROPERTY.onNext(Pair(Artifact.alias(),paraphrase.value))
                                KryptoStore.ALIAS_PROPERTY.onComplete()
                                true
                            } else {
                                false
                            }
                        } ui {
                            if (it) {
                                AppStore.AUTHSTATE.set(true)
                            } else {
                                errorMssg.apply {
                                    isVisible = true
                                    isManaged = true
                                    text= "password incorrect"
                                }
                                AppStore.AUTHSTATE.set(false)
                            }
                            // we have an alias so now attempt to configure credentials and bootstrap cloudtrail client
                            fire(LoginEvent())
                        } fail {
                            AppStore.AUTHSTATE.set(false)
                            fire(LoginEvent())
                        }
                    }
                }
                submit.apply {
                    VBox.setMargin(this, insets(20, 0, 0, 0))
                    addClass(LeftBarStyles.submitField)
                }
            }

            VBox.setMargin(this, insets(36, 0, 0, 0))
        }

        logOut = button("Log Out") {
            action {
                AppStore.AUTHSTATE.set(false)
                AppStore.IS_SCHEDLED.set(false)
                password.setValue("")
                paraphrase.setValue("")
                errorMssg.apply {
                    isVisible = false
                    isManaged = false
                }
            }
            addClass(LeftBarStyles.leftBarButton)
            VBox.setMargin(this, insets(20, 0, 0, 10))
        }

        filterState = gridpane {
            row {
                label("Schedule:").addClass(LeftBarStyles.filterFeedback)
                label(AppStore.SELECTED_SCHEDULE).addClass(LeftBarStyles.filterFeedbackValue)
            }
            row {
                label("IP:").addClass(LeftBarStyles.filterFeedback)
                label(model.ip).addClass(LeftBarStyles.filterFeedbackValue)
            }
            row {
                label("Type:").addClass(LeftBarStyles.filterFeedback)
                label(model.type).addClass(LeftBarStyles.filterFeedbackValue)
            }
            VBox.setMargin(this, insets(20, 0, 0, 10))
        }

        add(ipList)

        ipList.root.apply {
            style {
                padding = box(0.px)
            }
            VBox.setMargin(this, insets(20, 0, 0, 10))
        }

        logInContainer.visibleProperty().bind(!AppStore.AUTHSTATE)
        logInContainer.managedProperty().bind(!AppStore.AUTHSTATE)

        logOut.visibleProperty().bind(AppStore.AUTHSTATE)
        logOut.managedProperty().bind(AppStore.AUTHSTATE)

        filterState.visibleProperty().bind(AppStore.AUTHSTATE)
        filterState.managedProperty().bind(AppStore.AUTHSTATE)

        ipList.root.visibleProperty().bind(AppStore.AUTHSTATE)
        ipList.root.managedProperty().bind(AppStore.AUTHSTATE)

        addClass(LeftBarStyles.leftBar)
    }
}

