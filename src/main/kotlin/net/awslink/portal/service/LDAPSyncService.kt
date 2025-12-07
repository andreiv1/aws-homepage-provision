package net.awslink.portal.service

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class LDAPSyncService(
    private val ldapService: LDAPSearchService,
    private val portalGroupService: PortalGroupService,
    private val portalUserService: PortalUserService
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    // Runs every 10 minutes
    @Scheduled(fixedDelay = 600_000)
    fun scheduler() {
        this.logger.info("Starting LDAP synchronization task")
        syncLDAPGroups()
        syncLDAPUsers()
        this.logger.info("LDAP synchronization task completed")
    }

    private fun syncLDAPGroups() {
        val groups = ldapService.getAllGroups()
        portalGroupService.syncGroupsFromLDAP(groups)
    }

    private fun syncLDAPUsers() {
        val users = ldapService.getAllUsers()
        portalUserService.syncUsersFromLDAP(users);
    }
}