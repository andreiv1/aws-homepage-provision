package net.awslink.portal.model

import java.time.Instant

data class LDAPGroup(
    val name: String,
    val dn: String,
    val groupUuid: String? = null,
    val creationDate: Instant? = null,
    val modifiedDate: Instant? = null,
)
