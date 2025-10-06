package net.awslink.homepage_provision

import net.awslink.homepage_provision.service.LDAPService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
class HomepageProvisionApplication

fun main(args: Array<String>) {
	runApplication<HomepageProvisionApplication>(*args)

	class Test(ldapService: LDAPService) {

	}
}
