package net.awslink.portal.model

import java.time.Instant

data class LDAPUser(
    val id: String,
    val uuid: String? = null,
    val displayName: String? = null,
    val email: String,
    val groups: List<LDAPGroup> = emptyList(),
    val creationDate: Instant? = null,
    val modifiedDate: Instant? = null,
)
