package net.awslink.portal.service

import com.unboundid.ldap.sdk.Filter
import com.unboundid.ldap.sdk.LDAPInterface
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import net.awslink.portal.config.LDAPConfig
import net.awslink.portal.constants.ValueConstants.LDAP_CN
import net.awslink.portal.constants.ValueConstants.LDAP_CREATION_DATE
import net.awslink.portal.constants.ValueConstants.LDAP_DISPLAY_NAME
import net.awslink.portal.constants.ValueConstants.LDAP_DN
import net.awslink.portal.constants.ValueConstants.LDAP_EMAIL
import net.awslink.portal.constants.ValueConstants.LDAP_MEMBER_OF
import net.awslink.portal.constants.ValueConstants.LDAP_MODIFIED_DATE
import net.awslink.portal.constants.ValueConstants.LDAP_UID
import net.awslink.portal.constants.ValueConstants.LDAP_USER_TIME_FORMATTER
import net.awslink.portal.constants.ValueConstants.LDAP_UUID
import net.awslink.portal.model.LDAPGroup
import net.awslink.portal.model.LDAPUser
import net.awslink.portal.util.toInstant
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class LDAPSearchService(
    private val config: LDAPConfig,
    @Qualifier("ldapInterface") val ldap: LDAPInterface
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getAllUsers(groupCn: String? = null): List<LDAPUser> {
        val req = SearchRequest(
            config.base,
            SearchScope.SUB,
            Filter.createANDFilter(
                Filter.createEqualityFilter("objectClass", "inetOrgPerson"),
                if (groupCn != null) {
                    Filter.createEqualityFilter(LDAP_MEMBER_OF, groupCn)
                } else {
                    Filter.createPresenceFilter(LDAP_UID)
                }
            ),
            LDAP_UID, LDAP_UUID,
            LDAP_CN, LDAP_EMAIL, LDAP_DISPLAY_NAME, LDAP_MEMBER_OF, LDAP_CREATION_DATE, LDAP_MODIFIED_DATE
        )
        val res = ldap.search(req)

        return res.searchEntries.map { it.toUser() }
    }

    fun getAllGroups(): List<LDAPGroup> {
        val req = SearchRequest(
            config.base,
            SearchScope.SUB,
            Filter.createEqualityFilter("objectClass", "groupOfNames"),
            LDAP_CN, LDAP_DN, LDAP_CREATION_DATE, LDAP_MODIFIED_DATE, LDAP_UUID,
        )
        val res = ldap.search(req)

        return res.searchEntries.map { it.toGroup() }
    }

    private fun SearchResultEntry.toUser() = LDAPUser(
        id = this.getAttributeValue(LDAP_UID),
        uuid = this.getAttributeValue(LDAP_UUID),
        displayName = this.getAttributeValue(LDAP_DISPLAY_NAME) ?: this.getAttributeValue(LDAP_CN),
        email = this.getAttributeValue(LDAP_EMAIL) ?: "",
        groups = this.getAttributeValues(LDAP_MEMBER_OF)?.map {
            LDAPGroup(
                name = it.substringAfter("${LDAP_CN}=").substringBefore(","),
                dn = it
            )
        }?.sortedBy { it.name } ?: emptyList(),
        creationDate = this.getAttributeValue(LDAP_CREATION_DATE)?.toInstant(LDAP_USER_TIME_FORMATTER),
        modifiedDate = this.getAttributeValue(LDAP_MODIFIED_DATE)?.toInstant(LDAP_USER_TIME_FORMATTER)
    )

    private fun SearchResultEntry.toGroup() = LDAPGroup(
        name = this.getAttributeValue(LDAP_CN) ?: "",
        dn = this.dn,
        groupUuid = this.getAttributeValue(LDAP_UUID) ?: null,
        creationDate = this.getAttributeValue(LDAP_CREATION_DATE)?.toInstant(),
        modifiedDate = this.getAttributeValue(LDAP_MODIFIED_DATE)?.toInstant()
    )
}