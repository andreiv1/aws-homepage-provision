package net.awslink.portal.service

import net.awslink.portal.model.LDAPGroup
import net.awslink.portal.model.UserGroupResponse
import net.awslink.portal.repository.UserGroupRepository
import net.awslink.portal.repository.entity.UserGroup
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class UserGroupService(
    private val userGroupRepository: UserGroupRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getAllUserGroups(): List<UserGroupResponse> {
        return userGroupRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).map {
            it.toUserGroupResponse()
        }
    }

    fun getGroupsByLDAPGroups(ldapGroups: List<String>): List<UserGroup> {
        return userGroupRepository.findByLdapGroupIn(ldapGroups)
    }

    fun syncGroupsFromLDAP(ldapGroups: List<LDAPGroup>) {
        val existingGroups = userGroupRepository.findAll().associateBy { it.ldapGroup }
        val groupsToSave = ldapGroups.mapNotNull { ldapGroup ->
            if (existingGroups.containsKey(ldapGroup.name)) {
                null // Group already exists, no need to add
            } else {
                // Create new PortalGroup entity
                UserGroup(
                    name = ldapGroup.name,
                    description = null,
                    ldapDn = ldapGroup.dn,
                    ldapGroup = ldapGroup.name,
                    createDate = ldapGroup.creationDate,
                    updateDate = ldapGroup.modifiedDate
                )
            }
        }
        if(groupsToSave.isNotEmpty())  {
            userGroupRepository.saveAll(groupsToSave)
            logger.info("Synced ${groupsToSave.size} new LDAP groups")
        } else {
            logger.info("No new LDAP groups to sync")
        }
    }

    fun UserGroup.toUserGroupResponse(): UserGroupResponse {
        return UserGroupResponse(
            id = this.id!!,
            name = this.name,
            description = this.description ?: "",
            createDate = this.createDate.toString(),
            updateDate = this.updateDate.toString()
        )
    }
}