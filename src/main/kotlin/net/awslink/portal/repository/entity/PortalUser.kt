package net.awslink.portal.repository.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "portal_user")
class PortalUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(unique = true)
    val username: String = "",
    val displayName: String? = null,
    val email: String = "",
    @Column(unique = true)
    val ldapUuid: String? = null,
    val createDate: Instant? = Instant.now(),
    val updateDate: Instant? = null
){
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "portal_user_groups",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    var groups: MutableSet<PortalGroup> = mutableSetOf()

    val isLdapUser: Boolean
        get() = ldapUuid != null

}