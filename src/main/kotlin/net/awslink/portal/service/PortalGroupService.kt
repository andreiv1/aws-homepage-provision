package net.awslink.portal.service

import net.awslink.portal.model.LDAPGroup
import net.awslink.portal.repository.PortalGroupRepository
import net.awslink.portal.repository.entity.PortalGroup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PortalGroupService(
    private val portalGroupRepository: PortalGroupRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getGroupsByLDAPGroups(ldapGroups: List<String>): List<PortalGroup> {
        return portalGroupRepository.findByLdapGroupIn(ldapGroups)
    }

    fun syncGroupsFromLDAP(ldapGroups: List<LDAPGroup>) {
        val existingGroups = portalGroupRepository.findAll().associateBy { it.ldapGroup }
        val groupsToSave = ldapGroups.mapNotNull { ldapGroup ->
            if (existingGroups.containsKey(ldapGroup.name)) {
                null // Group already exists, no need to add
            } else {
                // Create new PortalGroup entity
                PortalGroup(
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
            portalGroupRepository.saveAll(groupsToSave)
            logger.info("Synced ${groupsToSave.size} new LDAP groups")
        } else {
            logger.info("No new LDAP groups to sync")
        }

    }
}