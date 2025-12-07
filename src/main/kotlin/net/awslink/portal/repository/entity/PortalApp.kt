package net.awslink.portal.repository.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "portal_app")
data class PortalApp(
    @Id
    val id: UUID,
    val name: String,
    val description: String? = null,
    val iconUrl: String? = null
) {
}