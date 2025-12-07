package net.awslink.portal.util

import java.time.Instant
import java.time.format.DateTimeFormatter

fun String.toInstant(formatter: DateTimeFormatter? = null): Instant? =
    runCatching {
        if(formatter != null) {
            return Instant.from(formatter.parse(this))
        } else {
            return Instant.parse(this)
        }
    }.getOrNull()
