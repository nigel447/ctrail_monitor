package com.aws.security

import javafx.stage.Screen
import tornadofx.*

abstract class StylesBase : Stylesheet() {
    val screenBounds = Screen.getPrimary().getVisualBounds()
    val darkBackgroundColor = "#00305A"
    val tabSelectedLabelColor = "tomato"
    val leftBarBackgroundColor = "lightsteelblue"
    val tabContentBackGroundColor = "mintcream"
}

class Styles: StylesBase()  {

    val textFillColor = "darkslateblue"
    val tabBorderColor ="#729EBF"

    init {
        root {
            prefWidth = Dimension(screenBounds.width / 1.1, Dimension.LinearUnits.px)
            prefHeight = Dimension(screenBounds.height / 1.3, Dimension.LinearUnits.px)
            fontSize = 13.px
        }

        headerLabel {
            fontFamily = "monospaced"
            fontSize = 28.px
            textFill = c(tabSelectedLabelColor)
        }

        appHeader {
            backgroundColor += c(darkBackgroundColor)
        }

        darkBorder {
            backgroundColor += c(darkBackgroundColor)
            borderWidth += box(0.px )
        }


        tabPane {
            borderColor += box(c(darkBackgroundColor))
            backgroundColor += c(this@Styles.tabContentBackGroundColor)
            // backgroundColor += c("#004B8D")

        }

        tabHeaderBackground {
            backgroundColor += c(this@Styles.darkBackgroundColor)

        }

        tabHeaderArea {
            backgroundColor += c("#729EBF")

        }

        tabContentArea {
            backgroundColor += c(this@Styles.tabContentBackGroundColor)
            borderColor += box(c("#729EBF"))
        }

        tab {
            borderColor += box(c(tabBorderColor))
            borderWidth += box(3.px)
            borderRadius += box(10.0.px, 10.0.px, 0.px, 0.0.px)
            backgroundRadius += box(14.0.px, 14.0.px, 0.px, 0.0.px)
            backgroundColor += c(this@Styles.tabContentBackGroundColor)

            and(selected) {
                tabLabel {
                    textFill = c(tabSelectedLabelColor)

                    focusColor = c("transparent")
                    faintFocusColor = c("transparent")
                    borderColor += box(c("transparent"))

                }
                focusColor = c("transparent")
                faintFocusColor = c("transparent")
                backgroundColor += c(this@Styles.tabContentBackGroundColor)
            }

            and(focused) {
                focusColor = c("transparent")
                faintFocusColor = c("transparent")
            }
        }

        tabLabel {
            textFill = c(textFillColor)
        }
    }

    companion object {
        val appHeader by cssclass()
        val headerLabel by cssclass()
        val darkBorder by cssclass()
    }
}