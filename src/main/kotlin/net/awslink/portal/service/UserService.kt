package net.awslink.portal.service

import net.awslink.portal.model.LDAPUser
import net.awslink.portal.model.UserGroupResponse
import net.awslink.portal.model.UserResponse
import net.awslink.portal.repository.UserRepository
import net.awslink.portal.repository.entity.User
import net.awslink.portal.repository.entity.UserGroup
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userGroupService: UserGroupService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getAllUsers(sort: Sort): List<UserResponse> {
        return userRepository.findAll(sort).map {
            it.toUserResponse()
        }
    }

    @Transactional
    fun syncUsersFromLDAP(ldapUsers: List<LDAPUser>) {
        val existingPortalUsersByUuid = userRepository.findAll().associateBy { it.ldapUuid }
        val ldapUsersByUuid = ldapUsers.associateBy { it.uuid }

        val removedUsersUuid = existingPortalUsersByUuid.keys - ldapUsersByUuid.keys
        val removedUsers: List<User> =
            removedUsersUuid.mapNotNull { uuid -> existingPortalUsersByUuid[uuid] }

        val newUsersUuid = ldapUsersByUuid.keys - existingPortalUsersByUuid.keys
        val newUsers: List<LDAPUser> =
            newUsersUuid.mapNotNull { uuid -> ldapUsersByUuid[uuid] }

        val existingUsersUuid = existingPortalUsersByUuid.keys intersect ldapUsersByUuid.keys
        val existingUsers: List<Pair<User, LDAPUser>> =
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
            val newUser = User(
                username = it.id,
                displayName = it.displayName,
                email = it.email,
                ldapUuid = it.uuid!!,
                createDate = it.creationDate,
                updateDate = it.modifiedDate
            )
            // Handle group assignments
            val groups = userGroupService.getGroupsByLDAPGroups(it.groups.map { g -> g.name })
            if (groups.isNotEmpty()) {
                logger.info("Assigning groups to user ${newUser.username}: ${groups.joinToString(", ") { g -> g.name }}")
                newUser.groups.addAll(groups)
            } else {
                logger.info("No groups found to assign to user ${newUser.username}")
            }
            userRepository.save(newUser)
            logger.info("Saved new user to database: Username=${newUser.username}, UUID=${newUser.ldapUuid}")
        }
    }

    private fun handleExistingUsers(
        existingUsers: List<Pair<User, LDAPUser>>
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
        existingUser: User,
        ldapUser: LDAPUser
    ): Boolean {
        val ldapGroups = ldapUser.groups.map { it.name }.toSet()
        val currentGroups = existingUser.groups.mapNotNull { it.ldapGroup }.toSet()

        val groupsToAdd = ldapGroups - currentGroups
        val groupsToRemove = currentGroups - ldapGroups

        var changed = false

        if (groupsToAdd.isNotEmpty()) {
            val groups = userGroupService.getGroupsByLDAPGroups(groupsToAdd.toList())
            existingUser.groups.addAll(groups)
            changed = true
        }

        if (groupsToRemove.isNotEmpty()) {
            val groups = userGroupService.getGroupsByLDAPGroups(groupsToRemove.toList())
            existingUser.groups.removeAll(groups.toSet())
            changed = true
        }
        return changed
    }

    private fun handleRemovedUsers(users: List<User>) {
        userRepository.deleteAll(users)
        users.forEach {
            logger.info("Removed LDAP user no longer present in LDAP: UUID=${it.ldapUuid}, Username=${it.username}")
        }
    }

    fun User.toUserResponse(): UserResponse {
        return UserResponse(
            id = this.id!!,
            displayName = this.displayName ?: "",
            email = this.email,
            username = this.username,
            createDate = this.createDate.toString(),
            updateDate = this.updateDate.toString()
        )
    }
}