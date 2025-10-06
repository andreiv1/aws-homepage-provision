package net.awslink.homepage_provision.service

import net.awslink.homepage_provision.model.User
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class HomepageProvisionService(
    private val ldapService: LDAPService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Runs every 10 minutes
    @Scheduled(fixedDelay = 600_000)
    fun scheduler() {
        this.logger.info("Starting homepage provision scheduler task")
        val users = ldapService.getAllUsers("cn=aws_user,ou=groups,dc=awslink,dc=net")
        users.forEach {
            provisionHomepageForUser(it)
        }
    }

    private fun provisionHomepageForUser(user: User) {
        // Implementation for provisioning homepage
        this.logger.warn("Provisioning homepage for user: $user")
    }
}