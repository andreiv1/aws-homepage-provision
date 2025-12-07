package net.awslink.portal.constants

import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object ValueConstants {
    val LDAP_USER_TIME_FORMATTER: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMddHHmmssX")
            .withZone(ZoneOffset.UTC)

    const val LDAP_UID = "uid"
    const val LDAP_DISPLAY_NAME = "displayName"
    const val LDAP_EMAIL = "mail"
    const val LDAP_MEMBER_OF = "memberOf"
    const val LDAP_CN = "cn"
    const val LDAP_DN = "dn"

    const val LDAP_UUID = "uuid"
    const val LDAP_CREATION_DATE = "creation_date"
    const val LDAP_MODIFIED_DATE = "modified_date"

}