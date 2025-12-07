package net.awslink.portal.repository

import net.awslink.portal.repository.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
}