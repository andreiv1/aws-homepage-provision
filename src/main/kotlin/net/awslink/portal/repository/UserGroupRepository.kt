package net.awslink.portal.repository

import net.awslink.portal.repository.entity.UserGroup
import org.springframework.data.jpa.repository.JpaRepository

interface UserGroupRepository : JpaRepository<UserGroup, Long> {
    fun findByLdapGroupIn(names: List<String>): List<UserGroup>
}