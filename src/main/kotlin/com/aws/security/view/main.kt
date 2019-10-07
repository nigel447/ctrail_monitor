package com.aws.security.view

import com.aws.security.*
import javafx.scene.Node
import javafx.scene.control.TabPane
import tornadofx.*


class MainScreen : View("Ctrail Secure Applications") {

    lateinit var topNode: Node
    lateinit var leftBarNode: Node
    lateinit var centerNode: Node

    init {
        subscribe<LoginEvent> { _ ->

            if (AppStore.AUTHSTATE.get()) {
                // we have a paraphrase for the keystore
                val encryptedCredsMap = KryptoArtifactsLoader.monitor()

                val decryptedCreds = KryptoProvider.deCryptAWSKeys(
                    Pair(
                        encryptedCredsMap.get("access")!!,
                        encryptedCredsMap.get("secret")!!
                    )
                )
                AppStore.DECRYPTED_ACCESS.set(decryptedCreds.first)
                AppStore.DECRYPTED_SECRET.set(decryptedCreds.second)
                CloudTrail.initClient()
            }
        }
    }

    override val root = borderpane {

        top<Header>()

        leftBarNode = left<LeftBar>()

        centerNode = center<Tabs>()

        centerNode.apply {
            addClass(Styles.darkBorder)
        }

        addClass(Styles.darkBorder)
    }
}


class Tabs : View() {

    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab("Event Monitor") {
            add<EventsMonitor>()
        }

    }

}