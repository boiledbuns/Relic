package com.relic.network.request

interface RelicRequestContract {}

sealed class RelicRequestError : Exception() {
    class NetworkUnavailableError: RelicRequestError()
}