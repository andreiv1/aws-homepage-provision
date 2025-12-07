package net.awslink.portal.repository

import net.awslink.portal.repository.entity.PortalUser
import org.springframework.data.jpa.repository.JpaRepository

interface PortalUserRepository : JpaRepository<PortalUser, Long> {
}