package com.aws.security.controllers

import com.aws.security.*
import javafx.animation.Animation
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.util.Duration
import tornadofx.Controller

import java.util.*

object DataCache {
    val cache =  TreeSet<MonitorData>(tableComparator)
    val data = FXCollections.observableArrayList<MonitorData>()
}

abstract class ControllerBase : Controller() {

    val fr = KeyFrame ( Duration.seconds(AppStore.SELECTED_SCHEDULE.get().delay),
        object: EventHandler<ActionEvent> {
            override fun handle(event: ActionEvent?) {
                processData()
            }
        }
    )

    abstract fun processData(): Boolean

    fun <T : Collection<MonitorData>> refreshTable(dataList: Collection<MonitorData>) {
        Platform.runLater {
            DataCache.data.clear()
            DataCache.data.addAll(dataList)
            fire(DataRefreshEvent())
        }
    }
}

class TableController : ControllerBase() {

    fun  processScheduledData() {
        processData()
        val runner = Timeline(fr)
        runner.setCycleCount(Animation.INDEFINITE)
        runner.play()
        AppStore.IS_SCHEDLED.set(true)

    }

    override fun processData(): Boolean {
        CloudTrail.monitor()
        CloudTrail.monitor().forEach {
            val evt = AppArtifacts.objectMapper.readValue(it.cloudTrailEvent, CTrailEvent::class.java)
            DataCache.cache.add(
                MonitorData(
                    evt.eventTime,
                    EventType.valueOf(evt.eventType.toUpperCase()),
                    evt.userIdentity!!.principalId,
                    evt.eventName,
                    evt.eventSource,
                    evt.sourceIPAddress,
                    evt.userAgent
                )
            )
        }
        refreshTable<List<MonitorData>>( DataCache.cache)
        return true
    }

}

class FilterController : ControllerBase() {


    val iPWhiteList = FXCollections.observableArrayList<String>()
    val iPCache = TreeSet<String>()
    init {
        subscribe<FilterEvent> { evt ->
            when (evt.type) {
                FilterEventType.FILTER_IP -> filterIp()
                else -> filterType()
            }
        }
    }

    fun updateWhiteList() {
        iPCache.add(AppStore.ENTERED_IP.get())
        iPWhiteList.clear()
        iPWhiteList.addAll(iPCache)
    }

    private fun filterIp() {

        val orderedFilterRet =  DataCache.cache.filterNot {
            it.sourceIPAddressProperty.get().trim().equals(AppStore.SELECTED_IP.get().trim())
        }
        AppArtifacts.appLogger.info("filtering on ip: ${AppStore.SELECTED_IP.get()}  with this many ret: ${orderedFilterRet.size}")
        refreshTable<List<MonitorData>>(orderedFilterRet)
    }

    private fun filterType() {
        val orderedFilterRet =  DataCache.cache.filter {
            it.typeProperty.get().toString().toUpperCase().trim().equals(AppStore.SELECTED_TYPE.get().toString().toUpperCase().trim())
        }
        AppArtifacts.appLogger.info("filtering on ip: ${AppStore.SELECTED_TYPE.get().toString().trim()}  with this many ret: ${orderedFilterRet.size}")
        refreshTable<List<MonitorData>>(orderedFilterRet)
    }

    override fun processData(): Boolean {
        return true
    }

}