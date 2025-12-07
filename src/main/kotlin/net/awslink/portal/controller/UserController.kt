package net.awslink.portal.controller

import net.awslink.portal.constants.URIConstants.API_V1_USERS
import net.awslink.portal.constants.URIConstants.API_V1_USERS_GROUPS
import net.awslink.portal.model.UserGroupResponse
import net.awslink.portal.model.UserResponse
import net.awslink.portal.service.UserGroupService
import net.awslink.portal.service.UserService
import org.springframework.data.domain.Sort

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
    private val userGroupService: UserGroupService,
) {
    @GetMapping(API_V1_USERS)
    fun getUsers(
        @RequestParam("sortBy", required = false) sortBy: String? = null,
        @RequestParam("sortOrder", required = false) sortOrder: String? = null,
    ): ResponseEntity<List<UserResponse>> {
        val safeSortBy = when (sortBy) {
            "username", "email", "createDate", "updateDate" -> sortBy
            null -> "username"
            else -> "username"
        }

        val direction = when {
            sortOrder.equals("desc", ignoreCase = true) -> Sort.Direction.DESC
            else -> Sort.Direction.ASC
        }

        val sort = Sort.by(direction, safeSortBy)
        return ResponseEntity.ok(userService.getAllUsers(sort))
    }

    @GetMapping(API_V1_USERS_GROUPS)
    fun getUserGroups(): ResponseEntity<List<UserGroupResponse>> {
        return ResponseEntity.ok(userGroupService.getAllUserGroups())
    }
}