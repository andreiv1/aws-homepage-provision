package net.awslink.homepage_provision.service

import com.unboundid.ldap.sdk.Filter
import com.unboundid.ldap.sdk.LDAPInterface
import com.unboundid.ldap.sdk.SearchRequest
import com.unboundid.ldap.sdk.SearchResultEntry
import com.unboundid.ldap.sdk.SearchScope
import net.awslink.homepage_provision.config.LDAPConfig
import net.awslink.homepage_provision.model.Group
import net.awslink.homepage_provision.model.User
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class LDAPService(
    private final val config: LDAPConfig,
    @Qualifier("ldapInterface") final val ldap: LDAPInterface
) {
    fun getAllUsers(groupCn: String? = null): List<User> {
        val req = SearchRequest(
            config.base,
            SearchScope.SUB,
            Filter.createANDFilter(
                Filter.createEqualityFilter("objectClass", "inetOrgPerson"),
                if (groupCn != null) {
                    Filter.createEqualityFilter("memberOf", groupCn)
                } else {
                    Filter.createPresenceFilter("uid")
                }
            ),
            "uid", "cn", "mail", "displayName", "memberOf"
        )
        val res = ldap.search(req)

        return res.searchEntries.map { it.toUser() }
    }

    private fun SearchResultEntry.toUser() = User(
        uid = this.getAttributeValue("uid") ?: "",
        displayName = this.getAttributeValue("displayName") ?: this.getAttributeValue("cn") ?: "",
        email = this.getAttributeValue("mail") ?: "",
        groups = this.getAttributeValues("memberOf")?.map {
            Group(name = it.substringAfter("cn=").substringBefore(","), dn = it)
        }?.sortedBy { it.name } ?: emptyList()
    )
}