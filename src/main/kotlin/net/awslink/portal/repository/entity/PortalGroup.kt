package net.awslink.portal.repository.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "portal_group")
class PortalGroup(
    @Column(unique = true)
    val name: String = "",
    val description: String? = null,
    @Column(unique = true)
    val ldapDn: String? = null,
    @Column(unique = true)
    val ldapGroup: String? = null,

    val createDate: Instant? = Instant.now(),

    val updateDate: Instant? = null
){
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}