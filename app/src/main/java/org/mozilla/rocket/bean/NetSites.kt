package org.mozilla.rocket.bean

data class NetSites (
    var netSites: List<NetSet>
)

data class NetSet(
    var sitName: String,
    var sitLink: String,
    var siteIcon: String
)