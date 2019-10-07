package com.aws.security

import com.aws.security.controllers.DataCache
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import tornadofx.eq
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FilterTest : ClientTestBase() {

    val filterIPs = mutableListOf("169.153.71.123")

    @Test
    fun collectionsTest() {

    //  String, EventType = EventType.OTHER, String?,  String,  String,  String, String

        val cache =  TreeSet<MonitorData>(tableComparator)

        cache.add(MonitorData(Instant.now().toString(), EventType.OTHER,"vv","vvvv","vvvvv","vvv","fff"))
    }



    @Test
    fun ipFilterTest() {

        val ret = CloudTrail.monitor()
        ret.forEach {
            val evt = AppArtifacts.objectMapper.readValue(it.cloudTrailEvent, CTrailEvent::class.java)
            DataCache.cache.add(
                MonitorData(
                    evt.eventTime,
                    EventType.valueOf(evt.userIdentity!!.type.toUpperCase()),
                    evt.userIdentity!!.principalId, evt.eventName, evt.eventSource, evt.sourceIPAddress, evt.userAgent
                )
            )
        }



//        val orderedFilterRet =timeOrderSet.filterNot {
//            it.sourceIPAddressProperty.eq(filterIPs.first()).get()
//        }
        DataCache.cache.forEach {
            val dt = LocalDateTime.ofInstant(Instant.parse(it.eventTimeProperty.get()), ZoneId.of("UTC"))
            val tableRow =
                "${it.typeProperty.get()}:::${dt.format(AppState.TABLE_TIME_FORMAT)}:::${it.sourceIPAddressProperty.get()}"
            println(tableRow)

        }
    }


}