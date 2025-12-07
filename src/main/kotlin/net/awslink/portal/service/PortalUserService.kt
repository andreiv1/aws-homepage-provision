package net.awslink.portal.service

import net.awslink.portal.model.LDAPUser
import net.awslink.portal.model.UserResponse
import net.awslink.portal.repository.PortalUserRepository
import net.awslink.portal.repository.entity.PortalUser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PortalUserService(
    private val portalUserRepository: PortalUserRepository,
    private val portalGroupService: PortalGroupService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getAllUsers(): List<UserResponse> {
        return portalUserRepository.findAll().map {
            UserResponse(
                id = it.id!!,
                displayName = it.displayName ?:"",
                email = it.email,
                username = it.username,
                createDate = it.createDate.toString(),
                updateDate = it.updateDate.toString()
            )
        }
    }

    @Transactional
    fun syncUsersFromLDAP(ldapUsers: List<LDAPUser>) {
        val existingPortalUsersByUuid = portalUserRepository.findAll().associateBy { it.ldapUuid }
        val ldapUsersByUuid = ldapUsers.associateBy { it.uuid }

        val removedUsersUuid = existingPortalUsersByUuid.keys - ldapUsersByUuid.keys
        val removedUsers: List<PortalUser> =
            removedUsersUuid.mapNotNull { uuid -> existingPortalUsersByUuid[uuid] }

        val newUsersUuid = ldapUsersByUuid.keys - existingPortalUsersByUuid.keys
        val newUsers: List<LDAPUser> =
            newUsersUuid.mapNotNull { uuid -> ldapUsersByUuid[uuid] }

        val existingUsersUuid = existingPortalUsersByUuid.keys intersect ldapUsersByUuid.keys
        val existingUsers: List<Pair<PortalUser, LDAPUser>> =
            existingUsersUuid.mapNotNull { uuid ->
                val portal = existingPortalUsersByUuid[uuid]
                val ldap = ldapUsersByUuid[uuid]
                if (portal != null && ldap != null) portal to ldap else null
            }

        handleRemovedUsers(removedUsers)
        handleNewUsers(newUsers)
        handleExistingUsers(existingUsers)
    }

    private fun handleNewUsers(newLDAPUsers: List<LDAPUser>) {
        newLDAPUsers.forEach {
            logger.info("New LDAP user found: UUID=${it.uuid}, Username=${it.id}, Email=${it.email}, DisplayName=${it.displayName}")
            val newUser = PortalUser(
                username = it.id,
                displayName = it.displayName,
                email = it.email,
                ldapUuid = it.uuid!!,
                createDate = it.creationDate,
                updateDate = it.modifiedDate
            )
            // Handle group assignments
            val groups = portalGroupService.getGroupsByLDAPGroups(it.groups.map { g -> g.name })
            if (groups.isNotEmpty()) {
                logger.info("Assigning groups to user ${newUser.username}: ${groups.joinToString(", ") { g -> g.name }}")
                newUser.groups.addAll(groups)
            } else {
                logger.info("No groups found to assign to user ${newUser.username}")
            }
            portalUserRepository.save(newUser)
            logger.info("Saved new user to database: Username=${newUser.username}, UUID=${newUser.ldapUuid}")
        }
    }

    private fun handleExistingUsers(
        existingUsers: List<Pair<PortalUser, LDAPUser>>
    ) {
        existingUsers.forEach { (portalUser, ldapUser) ->
            val groupsChanged = handleExistingUserGroups(portalUser, ldapUser)

            if (groupsChanged) {
                logger.info("Updating existing user in database: Username=${portalUser.username}, UUID=${portalUser.ldapUuid}")
                logger.info(" - Groups assigned to user: ${
                    portalUser.groups.joinToString(", ") { g -> g.name }
                }")
            }
        }
    }

    private fun handleExistingUserGroups(
        existingPortalUser: PortalUser,
        ldapUser: LDAPUser
    ): Boolean {
        val ldapGroups = ldapUser.groups.map { it.name }.toSet()
        val currentGroups = existingPortalUser.groups.mapNotNull { it.ldapGroup }.toSet()

        val groupsToAdd = ldapGroups - currentGroups
        val groupsToRemove = currentGroups - ldapGroups

        var changed = false

        if (groupsToAdd.isNotEmpty()) {
            val groups = portalGroupService.getGroupsByLDAPGroups(groupsToAdd.toList())
            existingPortalUser.groups.addAll(groups)
            changed = true
        }

        if (groupsToRemove.isNotEmpty()) {
            val groups = portalGroupService.getGroupsByLDAPGroups(groupsToRemove.toList())
            existingPortalUser.groups.removeAll(groups.toSet())
            changed = true
        }
        return changed
    }

    private fun handleRemovedUsers(portalUsers: List<PortalUser>) {
        portalUserRepository.deleteAll(portalUsers)
        portalUsers.forEach {
            logger.info("Removed LDAP user no longer present in LDAP: UUID=${it.ldapUuid}, Username=${it.username}")
        }
    }
}