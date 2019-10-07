package com.aws.security

import tornadofx.EventBus
import tornadofx.FXEvent

class LoginEvent : FXEvent()

class FilterEvent(val type: FilterEventType) : FXEvent(runOn = EventBus.RunOn.ApplicationThread)

class DataRefreshEvent : FXEvent(runOn = EventBus.RunOn.ApplicationThread)