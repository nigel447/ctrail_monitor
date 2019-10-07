package com.aws.security

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ItemViewModel
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

enum class EventType { IAMUSER, ROOT, AWSCONSOLESIGNIN, AWSAPICALL, AWSSERVICEEVENT, AWSSERVICE, OTHER}

enum class ScheduleType(val delay:Double, val mssg:String) {
    ONE_OFF(0.0, "One Off Refresh"),
    REPEATED_MIN(60.0, "1 Min Refresh"),
    REPEATED_15_MIN(15*60.0, "15 Min Refresh"),
    REPEATED_1HOURLY(60*60.0,"Hourly Refresh") }

enum class FilterEventType(var display: String) {
    FILTER_IP("WhiteList Selected IP"), FILTER_TYPE("Filter Type")
}

val  tableComparator  =  object : Comparator<MonitorData> {
    override fun compare(o1: MonitorData, o2: MonitorData): Int {
        return  Instant.parse(o2.eventTimeProperty.get())
            .compareTo( Instant.parse(o1.eventTimeProperty.get()) )
    }
}

class MonitorData(
    eventTime: String,
    type: EventType = EventType.OTHER,
    principalId: String?,
    eventName: String,
    eventSource: String,
    sourceIPAddress: String,
    userAgent: String
) {
    val eventTimeProperty = SimpleStringProperty(eventTime)
    val typeProperty = SimpleObjectProperty(type)
    val principalIdProperty = SimpleStringProperty(principalId)
    val eventNameProperty= SimpleStringProperty(eventName)
    val eventSourceProperty = SimpleStringProperty(eventSource)
    val sourceIPAddressProperty = SimpleStringProperty(sourceIPAddress)
    val userAgentProperty = SimpleStringProperty(userAgent)
}

class MonitorDataModel : ItemViewModel<MonitorData>() {
    val ip = bind { item?.sourceIPAddressProperty }
    val type = bind { item?.typeProperty }
}

// https://hceris.com/painless-json-with-kotlin-and-jackson/
@JsonIgnoreProperties(ignoreUnknown = true)
data class CTrailEvent(
    val userIdentity: UserIdentity?,
    val eventTime: String,
    val eventSource: String,
    val eventName: String,
    val awsRegion: String,
    val sourceIPAddress: String,
    val userAgent: String,
    val requestParameters: RequestParameters?,
    val eventType: String,
    val recipientAccountId: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class RequestParameters(val userName: String?, val serviceName: String?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class UserIdentity(
    val type: String,
    val principalId: String?,
    val arn: String?,
    val accountId: String?,
    val accessKeyId: String?,
    val sessionContext: SessionContext?,
    val invokedBy: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SessionContext(val attributes:Attributes? )

@JsonIgnoreProperties(ignoreUnknown = true)
data class Attributes( val mfaAuthenticated: String?, val creationDate: String)