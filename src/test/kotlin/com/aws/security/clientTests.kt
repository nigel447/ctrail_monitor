package com.aws.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

abstract class ClientTestBase {

    val timeData = "2019-10-01T11:03:55Z"

    private val krypto = setKrypto().apply {
        println(this)
    }


    fun setKrypto(): String {
        // in CTrailApp init
        AppState.bootStrap()

        // in login form submit
        KryptoStore.ALIAS_PROPERTY.onNext(Pair(Artifact.alias(), "some_alias"))
        KryptoStore.ALIAS_PROPERTY.onComplete()

        // ==== in MainScreen init subscribe login event ====
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
        // ====================================================
        return "CloudTrail initClient OK"
    }

}




// single instance of the test class is used for every method.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientTest : ClientTestBase() {

    @Test
    fun runClientEvent() {
        println("tests r go")

        CloudTrail.monitor().forEach {
            val evt = AppArtifacts.objectMapper.readValue(it.cloudTrailEvent, CTrailEvent::class.java)
            when(evt.userIdentity!!.type) {
              "IAMUser","Root" ->  {
                  println("usr evt  ${evt.userAgent}")
                  // data.add(MonitorData(evt.eventTime, evt.userIdentity!!.type, evt.userIdentity!!.principalId, evt.eventName, evt.eventSource, evt.sourceIPAddress, evt.userAgent))
              }
                else -> println("other  ${evt.eventType}    ${evt.userAgent}")
            }
            val type = EventType.valueOf(evt.eventType.toUpperCase())
            when(type) {
                EventType.AWSAPICALL, EventType.AWSCONSOLESIGNIN -> { }// do nothing
                else -> println(evt.eventType)

            }
            println(evt.sourceIPAddress)
        }


    }

    // http://en.wikipedia.org/wiki/ISO_8601#Combined_date_and_time_representations
    val ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    var formatter = DateTimeFormatter.ofPattern(ISO_8601_24H_FULL_FORMAT)
    @Test
    fun parseDateFormat() {

// https://stackoverflow.com/questions/2597083/illegal-pattern-character-t-when-parsing-a-date-string-to-java-util-date
        val timeVal = Instant.parse(timeData)
        val localDateTime = LocalDateTime.ofInstant(timeVal, ZoneId.of("UTC"))
        val humanTime = """event time is
            |${localDateTime.dayOfWeek}: ${localDateTime.month.name} ${localDateTime.dayOfMonth}  
            | at  hour  ${localDateTime.hour}  min ${localDateTime.minute}
        """.trimMargin()
        println(humanTime)

    }



}