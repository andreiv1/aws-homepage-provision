package net.awslink.portal.repository

import net.awslink.portal.repository.entity.App
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AppRepository : JpaRepository<App, UUID> {
}