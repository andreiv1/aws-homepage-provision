package net.awslink.portal.repository

import net.awslink.portal.repository.entity.PortalGroup
import org.springframework.data.jpa.repository.JpaRepository

interface PortalGroupRepository : JpaRepository<PortalGroup, Long> {
    fun findByLdapGroupIn(names: List<String>): List<PortalGroup>
}