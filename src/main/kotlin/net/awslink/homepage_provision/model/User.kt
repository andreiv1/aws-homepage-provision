package net.awslink.homepage_provision.model

data class User(
    val uid: String,
    val displayName: String?,
    val email: String,
    val groups: List<Group>
)
