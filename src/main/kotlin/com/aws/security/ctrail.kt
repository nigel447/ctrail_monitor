package com.aws.security

import com.aws.security.view.LeftBarStyles
import com.aws.security.view.MainScreen
import com.aws.security.view.WidgetStyles
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.observers.ResourceObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.stage.Stage
import org.apache.commons.logging.LogFactory
import org.bouncycastle.jce.provider.BouncyCastleProvider
import tornadofx.App
import tornadofx.UIComponent
import tornadofx.launch
import java.security.Security
import java.time.format.DateTimeFormatter


object AppArtifacts {
    val appLogger = LogFactory.getLog("krypto-manager")
    val objectMapper = jacksonObjectMapper().registerModule(KotlinModule())
    // strong password hash
    val BASIC_PSSWD =
        "5848aa2ae52bd3c0eed48d14da9aab5b1e1b46c72ba07a46eea23510ce2864dc093f0e143810240e71c5dd4d1fb4d292beeec0630b7a7f6f523fdca25e096adc"
}


object AppState {
    val IS_JPRO = true
    val TABLE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd HH:mm")
    fun bootStrap() {
        Security.addProvider(BouncyCastleProvider())
        KryptoStore.bootstrapKryptoStore()
        KryptoStore.createSub()
    }
}

object KryptoStore {
    val ACCESS_ID_PROPERTY: AsyncSubject<Pair<Artifact, String>> = AsyncSubject.create()
    val SECRET_KEY_PROPERTY: AsyncSubject<Pair<Artifact, String>> = AsyncSubject.create()
    val ALIAS_PROPERTY: AsyncSubject<Pair<Artifact, String>> = AsyncSubject.create()

    var KEY_STORE_PASSWD = ""
    var SYM_KEY_PSSWD = ""
    var JKS = ""

    var ACCESS_ID = ""
    var SECRET = ""
    var ALIAS = ""

    fun bootstrapKryptoStore() {
        val artifacts  =  KryptoArtifactsLoader.keyStoreWrapper()
        JKS =artifacts.keyStore
        KEY_STORE_PASSWD =artifacts.keyStorePasswd
        SYM_KEY_PSSWD =artifacts.symKePsswd

        ACCESS_ID_PROPERTY.subscribeOn(Schedulers.io()).subscribe(createSub())
        SECRET_KEY_PROPERTY.subscribeOn(Schedulers.io()).subscribe(createSub())
        ALIAS_PROPERTY.subscribeOn(Schedulers.io()).subscribe(createSub())
    }

    fun createSub(): ResourceObserver<Pair<Artifact, String>> {
        return KryptoSubscriber()
    }
}

object AppStore {
    var username = SimpleStringProperty("ctrail-monitor")
    val paraphrase = SimpleStringProperty()
    val DECRYPTED_ACCESS = SimpleStringProperty()
    val DECRYPTED_SECRET = SimpleStringProperty()
    val AUTHSTATE = SimpleBooleanProperty(false)
    val ENTERED_IP = SimpleStringProperty("")
    val SELECTED_IP = SimpleObjectProperty("")
    val SELECTED_TYPE = SimpleObjectProperty<EventType>()
    val SELECTED_SCHEDULE = SimpleObjectProperty<ScheduleType>(ScheduleType.ONE_OFF)
    val IS_SCHEDLED = SimpleBooleanProperty(false)
}

class CTrailApp : App(MainScreen::class, Styles::class, LeftBarStyles::class, WidgetStyles::class) {
    init {
        AppState.bootStrap()
        AppStore.SELECTED_SCHEDULE.addListener {
            obsv -> println("SELECTED_TYPE listener ${obsv}")
        }
    }

    override fun start(stage: Stage) {
        super.start(stage)
        stage.show()
    }
}

fun main(args: Array<String>) {
    launch<CTrailApp>(args)
}