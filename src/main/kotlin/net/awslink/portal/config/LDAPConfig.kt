package net.awslink.portal.config

import com.unboundid.ldap.sdk.*
import com.unboundid.util.ssl.SSLUtil
import jakarta.annotation.PreDestroy
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.context.annotation.Bean
import org.springframework.validation.annotation.Validated
import javax.net.ssl.SSLSocketFactory

@Validated
@ConfigurationProperties(prefix = "ldap")
data class LDAPConfig(
    val url: String,
    val base: String,
    val managerDn: String? = null,
    val managerPassword: String? = null,

    @DefaultValue("10000")
    val connectTimeout: Int = 10_000,
    @DefaultValue("10000")
    val readTimeout: Long = 10_000,
) {
    private var pool: LDAPConnectionPool? = null

    @Bean
    fun ldapInterface(): LDAPInterface {
        val endpoint = parseLdapsUrl(url)
        val sslUtil = SSLUtil()
        val sslSocketFactory: SSLSocketFactory = sslUtil.createSSLSocketFactory()

        val opts = LDAPConnectionOptions().apply {
            connectTimeoutMillis = connectTimeout
            responseTimeoutMillis = readTimeout
        }

        val serverSet = SingleServerSet(endpoint.host, endpoint.port, sslSocketFactory, opts)
        val bindRequest: BindRequest? = managerDn?.let { SimpleBindRequest(it, managerPassword ?: "") }

        pool = LDAPConnectionPool(serverSet, bindRequest, 1, 5).apply {
            healthCheck = GetEntryLDAPConnectionPoolHealthCheck(
                /* entryDN               = */ null,     // null => root DSE
                /* maxResponseTime       = */ 3000,     // ms
                /* invokeOnCreate        = */ true,
                /* invokeAfterAuthentication = */ true,
                /* invokeOnCheckout      = */ true,
                /* invokeOnRelease       = */ false,
                /* invokeForBackgroundChecks = */ true,
                /* invokeOnException     = */ true
            )
            setRetryFailedOperationsDueToInvalidConnections(true)
        }

        return pool!!
    }

    @PreDestroy
    fun shutdown() {
        pool?.close()
        pool = null
    }

    private data class Endpoint(val host: String, val port: Int)

    private fun parseLdapsUrl(u: String): Endpoint {
        require(u.lowercase().startsWith("ldap://") || u.lowercase().startsWith("ldaps://")) {
            "Only ldap:// or ldaps:// URLs are supported. Offending value: $u"
        }
        val noScheme = u.substringAfter("://")
        val hostPort = noScheme.substringBefore('/')           // strip path/query
        val (host, port) = if (':' in hostPort) {
            val parts = hostPort.split(':', limit = 2)
            parts[0] to parts[1].toInt()
        } else hostPort to 636
        require(host.isNotBlank()) { "Invalid ldaps URL (missing host): $u" }
        return Endpoint(host.trim(), port)
    }
}