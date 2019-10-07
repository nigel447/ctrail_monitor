package com.aws.security

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudtrail.AWSCloudTrail
import com.amazonaws.services.cloudtrail.AWSCloudTrailClientBuilder
import com.amazonaws.services.cloudtrail.model.Event
import com.amazonaws.services.cloudtrail.model.LookupEventsRequest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class Creds(val access: String, val secret: String) : AWSCredentials {

    override fun getAWSAccessKeyId(): String {
        return this.access
    }

    override fun getAWSSecretKey(): String {
        return this.secret
    }

    fun debug(): String {
        return "${this.access} ::: ${this.secret} "
    }
}

object CloudTrailClientProvider : AWSCredentialsProvider {

    val creds = Creds(AppStore.DECRYPTED_ACCESS.get(), AppStore.DECRYPTED_SECRET.get())

    fun client(): AWSCloudTrail {
        val builder = AWSCloudTrailClientBuilder.standard().withCredentials(this).withRegion(Regions.US_EAST_2)
        return builder.build()
    }
    override fun getCredentials(): AWSCredentials {
        return creds
    }

    override fun refresh() {
    }
}

object CloudTrail {
    lateinit var client: AWSCloudTrail

    fun initClient() {
        client = CloudTrailClientProvider.client()
    }
    fun monitor(): MutableList<Event> {
        val lookupEvtsReq = LookupEventsRequest()
        val zdt = ZonedDateTime.ofInstant(   Instant.now(), ZoneId.of("UTC"))
        val zdtBefore = zdt.minusDays(1L)
        lookupEvtsReq.startTime =  Date.from( zdtBefore.toInstant() )
        lookupEvtsReq.endTime = Date.from( zdt.toInstant() )
        val ret = client.lookupEvents(lookupEvtsReq)
        return ret.events
    }
}