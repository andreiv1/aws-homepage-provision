package net.awslink.portal.model

data class UserResponse(
    val id: Long,
    val displayName: String,
    val email: String,
    val username: String,
    val createDate: String,
    val updateDate: String
)
