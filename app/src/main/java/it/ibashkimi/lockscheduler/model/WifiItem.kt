package it.ibashkimi.lockscheduler.model

open class WifiItem(val ssid: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is WifiItem)
            return false
        return ssid == other.ssid
    }

    override fun hashCode(): Int {
        return ssid.hashCode()
    }

    override fun toString(): String {
        return "WifiItem[ssid=$ssid]"
    }
}
