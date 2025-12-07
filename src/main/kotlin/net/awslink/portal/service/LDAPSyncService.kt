package net.awslink.portal.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class LDAPSyncService(
    private val ldapService: LDAPSearchService,
    private val userGroupService: UserGroupService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Runs every 10 minutes
    @Scheduled(fixedDelay = 600_000)
    fun scheduler() =
        try {
            performSync()
        } catch (e: Exception) {
            this.logger.error("Error during LDAP synchronization task", e)
        }

    private fun performSync() {
        this.logger.info("Starting LDAP synchronization task")
        syncLDAPGroups()
        syncLDAPUsers()
        this.logger.info("LDAP synchronization task completed")
    }

    private fun syncLDAPGroups() {
        val groups = ldapService.getAllGroups()
        userGroupService.syncGroupsFromLDAP(groups)
    }

    private fun syncLDAPUsers() {
        val users = ldapService.getAllUsers()
        userService.syncUsersFromLDAP(users);
    }
}